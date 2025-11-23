package main.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import main.model.Payment;
import main.model.Property;
import main.model.enums.PaymentStatus;
import main.repository.PaymentRepository;
import main.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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

    public List<Payment> getPaymentsForProperty(UUID propertyId, UUID ownerId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Unauthorized");
        }

        return paymentRepository.findAllByContractPropertyId(propertyId);
    }

    public List<Payment> getPaymentsForTenant(UUID tenantId) {
        return paymentRepository.findAllByContractTenantIdOrderByDueDateDesc(tenantId);
    }

    @Transactional
    public void pay(UUID paymentId, UUID tenantId) {

        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!p.getContract().getTenant().getId().equals(tenantId)) {
            throw new SecurityException("Unauthorized payment attempt");
        }

        p.setStatus(PaymentStatus.SUCCESS);
        p.setPaidAt(LocalDateTime.now());

        paymentRepository.save(p);

        log.info("Payment {} marked as PAID by tenant {}", paymentId, tenantId);
    }
}
