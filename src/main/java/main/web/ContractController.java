package main.web;

import main.model.Property;
import main.security.UserData;
import main.service.ContractService;
import main.service.PropertyService;
import main.web.dto.ContractRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService contractService;
    private final PropertyService propertyService;

    @Autowired
    public ContractController(ContractService contractService, PropertyService propertyService) {
        this.contractService = contractService;
        this.propertyService = propertyService;
    }

    @GetMapping("/create/{propertyId}")
    public ModelAndView createPage(@PathVariable UUID propertyId) {

        Property property = propertyService.getById(propertyId);

        if (!property.getStatus().name().equals("AVAILABLE")) {
            return new ModelAndView("redirect:/properties/" + propertyId);
        }

        ModelAndView modelAndView = new ModelAndView("contracts/create");

        modelAndView.addObject("property", property);
        modelAndView.addObject("contractRequest", new ContractRequest());
        modelAndView.addObject("currentPath", "/contracts/create");

        return modelAndView;
    }

    @PostMapping("/create/{propertyId}")
    public String create(@PathVariable UUID propertyId,
                         @ModelAttribute ContractRequest request,
                         @AuthenticationPrincipal UserData user,
                         RedirectAttributes redirectAttributes) {

        try {
            contractService.create(propertyId, user.getUserId(), request);
            return "redirect:/tenant/dashboard";

        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/properties/" + propertyId;
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable UUID id,
                         @AuthenticationPrincipal UserData user) {

        contractService.cancel(id, user.getUserId());
        return "redirect:/tenant/dashboard";
    }

    @GetMapping("/history")
    public ModelAndView history(@AuthenticationPrincipal UserData user) {

        ModelAndView modelAndView = new ModelAndView("tenant/contract-history");

        modelAndView.addObject("history",
                contractService.getHistoryForTenant(user.getUserId()));

        modelAndView.addObject("currentPath", "/contracts/history");

        return modelAndView;
    }
}
