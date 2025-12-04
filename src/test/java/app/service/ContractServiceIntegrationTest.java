package app.service;

import app.model.Property;
import app.model.RentalContract;
import app.model.User;
import app.model.enums.Role;
import app.model.enums.Status;
import app.repository.PropertyRepository;
import app.repository.RentalContractRepository;
import app.repository.UserRepository;
import app.web.dto.ContractRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class ContractServiceIntegrationTest {

    @Autowired
    private ContractService contractService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private RentalContractRepository rentalContractRepository;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void create_createsContractAndMarksPropertyAsRented() {

        User owner = new User();
        owner.setUsername("owner1");
        owner.setFirstName("Owner");
        owner.setLastName("One");
        owner.setPhoneNumber("000000");
        owner.setEmail("owner@example.com");
        owner.setPassword("password");
        owner.setRole(Role.OWNER);
        owner = userRepository.save(owner);


        User tenant = new User();
        tenant.setUsername("tenant1");
        tenant.setFirstName("Tenant");
        tenant.setLastName("One");
        tenant.setPhoneNumber("111111");
        tenant.setEmail("tenant@example.com");
        tenant.setPassword("password");
        tenant.setRole(Role.TENANT);
        tenant = userRepository.save(tenant);


        Property property = new Property();
        property.setTitle("Nice apartment");
        property.setDescription("Test description");
        property.setCity("Sofia");
        property.setAddress("Test street 1");
        property.setBedrooms(2);
        property.setBathrooms(1);
        property.setAreaSqm(BigDecimal.valueOf(60));
        property.setMonthlyRent(BigDecimal.valueOf(800));
        property.setStatus(Status.AVAILABLE);
        property.setCreatedOn(LocalDateTime.now());
        property.setUpdatedOn(LocalDateTime.now());
        property.setOwner(owner);
        property = propertyRepository.save(property);

        ContractRequest request = new ContractRequest();
        request.setStartDate(LocalDate.now());


        contractService.create(property.getId(), tenant.getId(), request);

        List<RentalContract> contracts = rentalContractRepository.findAll();
        assertEquals(1, contracts.size());

        RentalContract contract = contracts.get(0);
        assertTrue(contract.isActive(), "Contract must be active after creation");
        assertEquals(tenant.getId(), contract.getTenant().getId());
        assertEquals(property.getId(), contract.getProperty().getId());

        Property updatedProperty = propertyRepository.findById(property.getId())
                .orElseThrow();
        assertEquals(Status.RENTED, updatedProperty.getStatus());
    }
}