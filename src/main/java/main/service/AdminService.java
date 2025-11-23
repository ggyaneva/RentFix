package main.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import main.feign.MaintenanceClient;
import main.model.Payment;
import main.model.Property;
import main.model.RentalContract;
import main.model.User;

import main.model.enums.Role;
import main.repository.PaymentRepository;
import main.repository.PropertyRepository;
import main.repository.RentalContractRepository;
import main.repository.UserRepository;
import main.web.dto.maintenance.MaintenanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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

    public List<User> getAllUsers() {
        log.info("Admin: fetching all users");
        return userRepository.findAll();
    }

    public List<MaintenanceResponse> getAllMaintenance() {
        log.info("Admin: fetching all maintenance tickets");
        //return maintenanceClient.getAll();
        return List.of();
    }


    public List<RentalContract> getAllContracts() {
        return rentalContractRepository.findAll();
    }

    public void deleteContract(UUID id) {
        rentalContractRepository.deleteById(id);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public void deletePayment(UUID id) {
        paymentRepository.deleteById(id);
    }


    public List<Property> getAllProperties() {
            return propertyRepository.findAll();
    }

    @Transactional
    public void deleteProperty(UUID id) {

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        rentalContractRepository.deleteAllByPropertyId(id);

        propertyRepository.delete(property);
    }


    @Transactional
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
                for (Payment p : payments) {
                    if (p.getContract().getTenant().getId().equals(id)) {
                        paymentRepository.delete(p);
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



