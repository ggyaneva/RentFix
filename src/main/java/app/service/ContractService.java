package app.service;

import app.exception.ResourceNotFoundException;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @CacheEvict(value = {"contract", "property"}, allEntries = true)
    public void create(UUID propertyId, UUID tenantId, ContractRequest request) {

        //Tenant must NOT have an active contract
        if (rentalContractRepository.existsByTenantIdAndActiveTrue(tenantId)) {
            throw new IllegalStateException("You already have an active contract.");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (property.getStatus() != Status.AVAILABLE) {
            throw new IllegalStateException("This property is already rented.");
        }

        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        RentalContract contract = new RentalContract();
        contract.setTenant(tenant);
        contract.setProperty(property);
        contract.setStartDate(request.getStartDate() == null ? LocalDate.now() : request.getStartDate());
        contract.setMonthlyRent(property.getMonthlyRent());
        contract.setActive(true);

        rentalContractRepository.save(contract);
        paymentService.createInitialPaymentsForNewContract(contract);
        paymentService.createFirstMonthlyRentPayment(contract);

        property.setStatus(Status.RENTED);
        propertyRepository.save(property);
        log.info("Contract created for tenant {} on property {}", tenantId, propertyId);
    }

    @Transactional
    @CacheEvict(value = {"contract", "property"}, allEntries = true)
    public void cancel(UUID contractId, UUID tenantId) {

        RentalContract contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException    ("Contract not found"));

        if (!contract.getTenant().getId().equals(tenantId)) {
            throw new SecurityException("Unauthorized");
        }

        if (!contract.isActive()) {
            return;
        }

        contract.setActive(false);
        contract.setEndDate(LocalDate.now());
        rentalContractRepository.save(contract);
        Property property = contract.getProperty();
        property.setStatus(Status.AVAILABLE);
        propertyRepository.save(property);
        log.info("Tenant {} moved out of contract {}", tenantId, contractId);
    }

    @Cacheable(value = "contract", key = "#root.methodName + '_' + #tenantId")
    public RentalContract getActiveContract(UUID tenantId) {
        return rentalContractRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElse(null);
    }

    @Cacheable(value = "contract", key = "#root.methodName + '_' + #tenantId")
    public List<RentalContract> getHistoryForTenant(UUID tenantId) {
        return rentalContractRepository.findByTenantIdOrderByStartDateDesc(tenantId);
    }

    @Cacheable(value = "contract", key = "#root.methodName + '_' + #propertyId")
    public List<RentalContract> getByProperty(UUID propertyId) {
        return rentalContractRepository.findByPropertyId(propertyId);
    }

    @Cacheable(value = "contract", key = "#root.methodName + '_' + #contractId")
    public RentalContract getById(UUID contractId) {
        return rentalContractRepository.getById(contractId);
    }

    @Cacheable(value = "contract", key = "#root.methodName + '_' + #tenantId")
    public List<RentalContract> getFullHistoryForTenant(UUID tenantId) {
        return rentalContractRepository.findFullHistoryForTenant(tenantId);
    }

    @Cacheable(value = "contract", key = "#root.methodName + '_' + #userId")
    public UUID getActivePropertyId(UUID userId) {

        RentalContract contract = rentalContractRepository
                .findByTenantIdAndActiveTrue(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Tenant does not have an active rental contract"));

        return contract.getProperty().getId();
    }


}
