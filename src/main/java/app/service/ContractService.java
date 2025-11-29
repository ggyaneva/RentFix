package app.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import app.model.Property;
import app.model.RentalContract;
import app.model.User;
import app.model.enums.Status;
import app.repository.PropertyRepository;
import app.repository.RentalContractRepository;
import app.repository.UserRepository;
import app.web.dto.ContractRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ContractService {

    private final RentalContractRepository rentalContractRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    @Autowired
    public ContractService(RentalContractRepository rentalContractRepository, PropertyRepository propertyRepository, UserRepository userRepository, PaymentService paymentService) {
        this.rentalContractRepository = rentalContractRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public void create(UUID propertyId, UUID tenantId, ContractRequest request) {

        //Tenant must NOT have an active contract
        if (rentalContractRepository.existsByTenantIdAndActiveTrue(tenantId)) {
            throw new IllegalStateException("You already have an active contract.");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (property.getStatus() != Status.AVAILABLE) {
            throw new IllegalStateException("This property is already rented.");
        }

        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        RentalContract c = new RentalContract();
        c.setTenant(tenant);
        c.setProperty(property);
        c.setStartDate(request.getStartDate() == null ? LocalDate.now() : request.getStartDate());
        c.setMonthlyRent(property.getMonthlyRent());
        c.setActive(true);

        rentalContractRepository.save(c);
        paymentService.createInitialPaymentsForNewContract(c);
        paymentService.createFirstMonthlyRentPayment(c);

        property.setStatus(Status.RENTED);
        propertyRepository.save(property);
        log.info("Contract created for tenant {} on property {}", tenantId, propertyId);
    }

    @Transactional
    public void cancel(UUID contractId, UUID tenantId) {

        RentalContract c = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        if (!c.getTenant().getId().equals(tenantId)) {
            throw new SecurityException("Unauthorized");
        }

        if (!c.isActive()) {
            return;
        }

        c.setActive(false);
        c.setEndDate(LocalDate.now());
        rentalContractRepository.save(c);
        Property property = c.getProperty();
        property.setStatus(Status.AVAILABLE);
        propertyRepository.save(property);
        log.info("Tenant {} moved out of contract {}", tenantId, contractId);
    }

    public RentalContract getActiveContract(UUID tenantId) {
        return rentalContractRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElse(null);
    }

    public List<RentalContract> getHistoryForTenant(UUID tenantId) {
        return rentalContractRepository.findByTenantIdOrderByStartDateDesc(tenantId);
    }

    public List<RentalContract> getByProperty(UUID id) {
        return rentalContractRepository.findByPropertyId(id);
    }


    public RentalContract getById(UUID contractId) {
        return rentalContractRepository.getById(contractId);
    }
    public List<RentalContract> getFullHistoryForTenant(UUID tenantId) {
        return rentalContractRepository.findFullHistoryForTenant(tenantId);
    }

}
