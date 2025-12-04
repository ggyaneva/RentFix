package app.web;

import app.model.Payment;
import app.model.Property;
import app.model.RentalContract;
import app.security.UserData;
import app.service.ContractService;
import app.service.MaintenanceFacade;
import app.service.PaymentService;
import app.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/owner")
public class OwnerDashboardController {

    private final PropertyService propertyService;
    private final ContractService contractService;
    private final PaymentService paymentService;
    private final MaintenanceFacade maintenanceFacade;

    @Autowired
    public OwnerDashboardController(PropertyService propertyService, ContractService contractService, PaymentService paymentService, MaintenanceFacade maintenanceFacade) {
        this.propertyService = propertyService;
        this.contractService = contractService;
        this.paymentService = paymentService;
        this.maintenanceFacade = maintenanceFacade;
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard(@AuthenticationPrincipal UserData owner) {

        List<Property> properties = propertyService.getByOwner(owner.getUserId());

        int maintenanceCount = maintenanceFacade.getCountForOwner(owner.getUserId());
        Map<UUID, Long> maintenanceCountMap =
                maintenanceFacade.getCountsByPropertyForOwner(owner.getUserId());

        ModelAndView modelAndView = new ModelAndView("owner/dashboard");
        modelAndView.addObject("properties", properties);
        modelAndView.addObject("maintenanceCount", maintenanceCount);
        modelAndView.addObject("maintenanceCountMap", maintenanceCountMap);
        modelAndView.addObject("currentPath", "/owner/dashboard");
        return modelAndView;
    }


    @GetMapping("/property/{id}/contracts")
    public ModelAndView propertyContracts(@PathVariable UUID id,
                                    @AuthenticationPrincipal UserData user) {

        Property property = propertyService.getByIdForOwner(id, user.getUserId()); // but now returns Property
        List<RentalContract> contracts = contractService.getByProperty(id);
        ModelAndView modelAndView = new ModelAndView("owner/property-contracts");

        modelAndView.addObject("property", property);
        modelAndView.addObject("contracts", contracts);
        modelAndView.addObject("currentPath", "/owner/dashboard");

        return modelAndView;
    }

    @GetMapping("/property/{id}/payments")
    public ModelAndView payments(@PathVariable UUID id,
                           @AuthenticationPrincipal UserData user) {

        Property property = propertyService.getByIdForOwner(id, user.getUserId());
        ModelAndView modelAndView = new ModelAndView("owner/property-payments");
        modelAndView.addObject("property", property);
        modelAndView.addObject("payments", paymentService.getPaymentsForProperty(id, user.getUserId()));
        modelAndView.addObject("currentPath", "/owner/dashboard");

        return modelAndView;
    }


    @GetMapping("/contract/payments/{contractId}")
    public ModelAndView contractPayments(@PathVariable UUID contractId) {

        RentalContract contract = contractService.getById(contractId);
        List<Payment> payments = paymentService.getByContract(contractId);

        ModelAndView modelAndView = new ModelAndView("owner/contract-payments");
        modelAndView.addObject("contract", contract);
        modelAndView.addObject("payments", payments);

        return modelAndView;
    }
}
