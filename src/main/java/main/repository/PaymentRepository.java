package main.repository;

import main.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByContractPropertyId(UUID propertyId);

    List<Payment> findAllByContractTenantIdOrderByDueDateDesc(UUID tenantId);

    boolean existsByContractIdAndDueDate(UUID contractId, LocalDate dueDate);
}
