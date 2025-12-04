package app.web;

import app.feign.dto.MaintenanceView;
import app.model.enums.MaintenanceStatus;
import app.security.UserData;
import app.service.MaintenanceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/owner/maintenance")
public class OwnerMaintenanceController {

    private final MaintenanceFacade maintenanceFacade;

    @Autowired
    public OwnerMaintenanceController(MaintenanceFacade maintenanceFacade) {
        this.maintenanceFacade = maintenanceFacade;
    }

    @GetMapping
    public ModelAndView list(@AuthenticationPrincipal UserData owner) {

        ModelAndView modelAndView = new ModelAndView("owner/maintenance-list");

        List<MaintenanceView> requests = maintenanceFacade.getForOwner(owner.getUserId());

        modelAndView.addObject("requests", requests);
        modelAndView.addObject("currentPath", "/owner/maintenance");

        return modelAndView;
    }

    @PostMapping("/{id}/status")
    public ModelAndView updateStatus(@PathVariable UUID id,
                                     @RequestParam String status) {

        MaintenanceStatus newStatus = MaintenanceStatus.valueOf(status);
        maintenanceFacade.updateStatus(id, newStatus);

        return new ModelAndView("redirect:/owner/maintenance");
    }
}
