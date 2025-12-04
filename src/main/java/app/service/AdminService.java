package app.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import app.feign.MaintenanceClient;
import app.model.Payment;
import app.model.Property;
import app.model.RentalContract;
import app.model.User;

import app.model.enums.PaymentStatus;
import app.model.enums.Role;
import app.model.enums.Status;
import app.repository.PaymentRepository;
import app.repository.PropertyRepository;
import app.repository.RentalContractRepository;
import app.repository.UserRepository;
import app.feign.dto.MaintenanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final MaintenanceClient maintenanceClient;
    private final RentalContractRepository rentalContractRepository;
    private final PaymentRepository paymentRepository;
    private final PropertyRepository propertyRepository;
    private final ContractService contractService;
    private final PropertyService propertyService;

    @Autowired
    public AdminService(UserRepository userRepository, MaintenanceClient maintenanceClient, RentalContractRepository rentalContractRepository, PaymentRepository paymentRepository, PropertyRepository propertyRepository, ContractService contractService, PropertyService propertyService) {
        this.userRepository = userRepository;
        this.maintenanceClient = maintenanceClient;
        this.rentalContractRepository = rentalContractRepository;
        this.paymentRepository = paymentRepository;
        this.propertyRepository = propertyRepository;
        this.contractService = contractService;
        this.propertyService = propertyService;
    }

    @Cacheable("admin")
    public List<User> getAllUsers() {
        log.info("Admin: fetching all users");
        return userRepository.findAll();
    }

    @Cacheable("admin")
    public List<MaintenanceResponse> getAllMaintenance() {
        log.info("Admin: fetching all maintenance tickets");
        return maintenanceClient.getAll();

    }

    @Cacheable("admin")
    public List<RentalContract> getAllContracts() {
        return rentalContractRepository.findAllSorted();
    }

    @Transactional
    @CacheEvict(value = "admin", allEntries = true)
    public void endContract(UUID contractId) {
        RentalContract contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        if (!contract.isActive()) return;

        contract.setActive(false);
        contract.setEndDate(LocalDate.now());

        List<Payment> futurePayments = paymentRepository.getContractPaymentsSorted(contractId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .toList();

        for (Payment payment : futurePayments) {
            payment.setStatus(PaymentStatus.CANCELED);
        }

        contract.getProperty().setStatus(Status.AVAILABLE);

        rentalContractRepository.save(contract);
    }

    @Cacheable("admin")
    public List<Payment> getAllPayments() {
        return paymentRepository.findAllSorted();
    }

    @Transactional
    @CacheEvict(value = "admin", allEntries = true)
    public void correctPayment(UUID id, BigDecimal amount, PaymentStatus status) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setAmount(amount);
        payment.setStatus(status);

        if (status == PaymentStatus.SUCCESS && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }

        if (status == PaymentStatus.PENDING) {
            payment.setPaidAt(null);
        }

        paymentRepository.save(payment);
    }

    @Cacheable("admin")
    public List<Property> getAllProperties() {
            return propertyRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "admin", allEntries = true)
    public void deleteProperty(UUID id) {

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        rentalContractRepository.deleteAllByPropertyId(id);

        propertyRepository.delete(property);
    }


    @Transactional
    @CacheEvict(value = "admin", allEntries = true)
    public void deleteUser(UUID id) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (user.getRole() == Role.ADMIN) {
                throw new IllegalStateException("Cannot delete ADMIN.");
            }

            if (user.getRole() == Role.TENANT) {
                List<RentalContract> contracts =
                        rentalContractRepository.findByTenantId(id);

                for (RentalContract c : contracts) {
                    if (c.isActive()) {
                        contractService.cancel(c.getId(), id);
                    }
                    rentalContractRepository.delete(c);
                }

                List<Payment> payments = paymentRepository.findAll();
                for (Payment payment : payments) {
                    if (payment.getContract().getTenant().getId().equals(id)) {
                        paymentRepository.delete(payment);
                    }
                }
            }

            if (user.getRole() == Role.OWNER) {

                List<Property> properties = propertyRepository.findAllByOwnerId(id);

                for (Property p : properties) {
                    propertyService.delete(p.getId(), id);
                }
            }
            userRepository.delete(user);
        }
    }



