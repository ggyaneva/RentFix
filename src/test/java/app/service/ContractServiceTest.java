package app.service;

import app.model.Property;
import app.model.RentalContract;
import app.repository.RentalContractRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private RentalContractRepository rentalContractRepository;

    @InjectMocks
    private ContractService contractService;

    @Test
    void getActivePropertyId_returnsPropertyId_whenActiveContractExists() {
        UUID tenantId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        Property property = new Property();
        property.setId(propertyId);

        RentalContract contract = new RentalContract();
        contract.setProperty(property);

        when(rentalContractRepository.findByTenantIdAndActiveTrue(tenantId))
                .thenReturn(Optional.of(contract));

        UUID result = contractService.getActivePropertyId(tenantId);

        assertEquals(propertyId, result);
        verify(rentalContractRepository).findByTenantIdAndActiveTrue(tenantId);
    }

    @Test
    void getActivePropertyId_throwsIllegalState_whenNoActiveContract() {
        UUID tenantId = UUID.randomUUID();

        when(rentalContractRepository.findByTenantIdAndActiveTrue(tenantId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> contractService.getActivePropertyId(tenantId));

        verify(rentalContractRepository).findByTenantIdAndActiveTrue(tenantId);
    }
}

