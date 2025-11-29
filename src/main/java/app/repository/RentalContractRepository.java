package app.repository;

import app.model.RentalContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentalContractRepository extends JpaRepository<RentalContract, UUID> {

    List<RentalContract> findByPropertyId(UUID propertyId);

    boolean existsByTenantIdAndActiveTrue(UUID tenantId);

    void deleteAllByPropertyId(UUID id);

    List<RentalContract> findByActiveTrue();

    List<RentalContract> findByTenantIdOrderByStartDateDesc(UUID userId);

    Optional<RentalContract> findByTenantIdAndActiveTrue(UUID tenantId);

    List<RentalContract> findByTenantId(UUID id);

    @Query("""
            SELECT c FROM RentalContract c
            WHERE c.tenant.id = :tenantId
            ORDER BY 
                CASE WHEN c.active = true THEN 0 ELSE 1 END,
                c.endDate DESC
            """)
    List<RentalContract> findFullHistoryForTenant(UUID tenantId);

    @Query("""
            SELECT c FROM RentalContract c
            ORDER BY
                CASE WHEN c.active = true THEN 0 ELSE 1 END,
                CASE 
                    WHEN c.active = true THEN c.startDate 
                    ELSE c.endDate 
                END DESC
            """)
    List<RentalContract> findAllSorted();


}
