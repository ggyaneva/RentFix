package app.repository;

import app.model.Payment;
import app.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("""
            SELECT p FROM Payment p
            WHERE p.contract.tenant.id = :tenantId
            ORDER BY 
                CASE WHEN p.status = 'PENDING' THEN 0 ELSE 1 END,
                p.paidAt DESC
            """)
    List<Payment> getTenantPaymentsSorted(UUID tenantId);


    @Query("""
            SELECT p FROM Payment p
            WHERE p.contract.property.id = :propertyId
            ORDER BY 
                CASE WHEN p.status = 'PENDING' THEN 0 ELSE 1 END,
                p.paidAt DESC
            """)
    List<Payment> getOwnerPaymentsSorted(UUID propertyId);


    @Query("""
            SELECT p FROM Payment p
            WHERE p.contract.id = :contractId
            ORDER BY 
                CASE WHEN p.status = 'PENDING' THEN 0 ELSE 1 END,
                p.paidAt DESC
            """)
    List<Payment> getContractPaymentsSorted(UUID contractId);

    @Query("""
            SELECT p FROM Payment p
            ORDER BY 
                p.dueDate DESC,
                p.paidAt DESC
            """)
    List<Payment> findAllSorted();


    boolean existsByContractIdAndTypeAndDueDate(UUID id, PaymentType paymentType, LocalDate nextDue);
}
