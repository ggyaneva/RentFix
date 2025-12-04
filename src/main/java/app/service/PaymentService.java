package app.service;

import app.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import app.model.Payment;
import app.model.Property;
import app.model.RentalContract;
import app.model.enums.PaymentStatus;
import app.model.enums.PaymentType;
import app.repository.PaymentRepository;
import app.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PropertyRepository propertyRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, PropertyRepository propertyRepository) {
        this.paymentRepository = paymentRepository;
        this.propertyRepository = propertyRepository;
    }

    @Cacheable("payment")
    public List<Payment> getPaymentsForTenant(UUID tenantId) {
        return paymentRepository.getTenantPaymentsSorted(tenantId);
    }

    @Cacheable("payment")
    public List<Payment> getPaymentsForProperty(UUID propertyId, UUID ownerId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Unauthorized");
        }

        return paymentRepository.getOwnerPaymentsSorted(propertyId);
    }

    @Cacheable("payment")
    public List<Payment> getByContract(UUID contractId) {
        return paymentRepository.getContractPaymentsSorted(contractId);
    }


    @Transactional
    @CacheEvict(value = "payment", allEntries = true)
    public void pay(UUID paymentId, UUID tenantId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getContract().getTenant().getId().equals(tenantId)) {
            throw new SecurityException("Unauthorized payment attempt");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);

        log.info("Payment {} marked as PAID by tenant {}", paymentId, tenantId);
    }

    @Transactional
    @CacheEvict(value = "payment", allEntries = true)
    public void createInitialPaymentsForNewContract(RentalContract contract) {

        BigDecimal rent = contract.getMonthlyRent();
        LocalDate start = contract.getStartDate();

        createPaidPayment(contract, rent, start, PaymentType.INITIAL_RENT);
        createPaidPayment(contract, rent, start, PaymentType.DEPOSIT);

    }

    @Transactional
    @CacheEvict(value = "payment", allEntries = true)
    public void createFirstMonthlyRentPayment(RentalContract contract) {

        BigDecimal rent = contract.getMonthlyRent();
        LocalDate dueDate = contract.getStartDate().plusMonths(1);

        createPayment(contract, rent, dueDate, PaymentType.MONTHLY_RENT);
    }


    private void createPayment(RentalContract contract,
                               BigDecimal amount,
                               LocalDate dueDate,
                               PaymentType type) {

        Payment payment = new Payment();
        payment.setContract(contract);
        payment.setAmount(amount);
        payment.setDueDate(dueDate);
        payment.setPaidAt(null);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setType(type);

        paymentRepository.save(payment);
    }

    private void createPaidPayment(RentalContract contract,
                                   BigDecimal amount,
                                   LocalDate dueDate,
                                   PaymentType type) {

        Payment payment = new Payment();
        payment.setContract(contract);
        payment.setAmount(amount);
        payment.setDueDate(dueDate);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setType(type);

        paymentRepository.save(payment);
    }

}

