package app.service;

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

    public List<Payment> getPaymentsForTenant(UUID tenantId) {
        return paymentRepository.getTenantPaymentsSorted(tenantId);
    }

    public List<Payment> getPaymentsForProperty(UUID propertyId, UUID ownerId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Unauthorized");
        }

        return paymentRepository.getOwnerPaymentsSorted(propertyId);
    }

    public List<Payment> findByContract(UUID contractId) {
        return paymentRepository.getContractPaymentsSorted(contractId);
    }


    @Transactional
    public void pay(UUID paymentId, UUID tenantId) {

        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!p.getContract().getTenant().getId().equals(tenantId)) {
            throw new SecurityException("Unauthorized payment attempt");
        }

        if (p.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        p.setStatus(PaymentStatus.SUCCESS);
        p.setPaidAt(LocalDateTime.now());

        paymentRepository.save(p);

        log.info("Payment {} marked as PAID by tenant {}", paymentId, tenantId);
    }

    @Transactional
    public void createInitialPaymentsForNewContract(RentalContract contract) {

        BigDecimal rent = contract.getMonthlyRent();
        LocalDate start = contract.getStartDate();

        createPaidPayment(contract, rent, start, PaymentType.INITIAL_RENT);
        createPaidPayment(contract, rent, start, PaymentType.DEPOSIT);

    }

    @Transactional
    public void createFirstMonthlyRentPayment(RentalContract contract) {

        BigDecimal rent = contract.getMonthlyRent();
        LocalDate dueDate = contract.getStartDate().plusMonths(1);

        createPayment(contract, rent, dueDate, PaymentType.MONTHLY_RENT);
    }


    private void createPayment(RentalContract contract,
                               BigDecimal amount,
                               LocalDate dueDate,
                               PaymentType type) {

        Payment p = new Payment();
        p.setContract(contract);
        p.setAmount(amount);
        p.setDueDate(dueDate);
        p.setPaidAt(null);
        p.setStatus(PaymentStatus.PENDING);
        p.setType(type);

        paymentRepository.save(p);
    }

    private void createPaidPayment(RentalContract contract,
                                   BigDecimal amount,
                                   LocalDate dueDate,
                                   PaymentType type) {

        Payment p = new Payment();
        p.setContract(contract);
        p.setAmount(amount);
        p.setDueDate(dueDate);
        p.setStatus(PaymentStatus.SUCCESS);
        p.setPaidAt(LocalDateTime.now());
        p.setType(type);

        paymentRepository.save(p);
    }

}

