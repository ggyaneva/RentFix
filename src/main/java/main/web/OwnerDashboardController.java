package main.web;

import main.model.Property;
import main.model.RentalContract;
import main.security.UserData;
import main.service.ContractService;
import main.service.PaymentService;
import main.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/owner")
public class OwnerDashboardController {

    private final PropertyService propertyService;
    private final ContractService contractService;
    private final PaymentService paymentService;

    @Autowired
    public OwnerDashboardController(PropertyService propertyService, ContractService contractService, PaymentService paymentService) {
        this.propertyService = propertyService;
        this.contractService = contractService;
        this.paymentService = paymentService;
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard(@AuthenticationPrincipal UserData user) {

        List<Property> properties = propertyService.getByOwner(user.getUserId());
        ModelAndView modelAndView = new ModelAndView("owner/dashboard");
        modelAndView.addObject("properties", properties);
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

}
