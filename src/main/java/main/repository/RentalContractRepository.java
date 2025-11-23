package main.repository;

import main.model.RentalContract;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<RentalContract> findAllByTenantIdAndActiveFalse(UUID tenantId);
}
