package app.web;

import app.feign.dto.MaintenanceCreateRequest;
import app.feign.dto.MaintenanceView;
import app.security.UserData;
import app.service.MaintenanceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/tenant/maintenance")
public class TenantMaintenanceController {

    private final MaintenanceFacade maintenanceFacade;

    @Autowired
    public TenantMaintenanceController(MaintenanceFacade maintenanceFacade) {
        this.maintenanceFacade = maintenanceFacade;
    }

    @GetMapping
    public ModelAndView list(@AuthenticationPrincipal UserData tenant) {

        ModelAndView modelAndView = new ModelAndView("tenant/maintenance-list");

        List<MaintenanceView> requests = maintenanceFacade.getForTenant(tenant.getUserId());

        modelAndView.addObject("requests", requests);
        modelAndView.addObject("createRequest", new MaintenanceCreateRequest());
        modelAndView.addObject("currentPath", "/tenant/maintenance");

        return modelAndView;
    }

    @PostMapping("/create")
    public ModelAndView create(@AuthenticationPrincipal UserData tenant,
                               @ModelAttribute("createRequest") MaintenanceCreateRequest form) {

        maintenanceFacade.createForTenant(tenant.getUserId(), form.getDescription());

        return new ModelAndView("redirect:/tenant/maintenance");
    }
}
