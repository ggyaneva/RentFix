package app.web;

import app.service.MaintenanceFacade;
import app.feign.dto.MaintenanceView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/maintenance")
public class AdminMaintenanceController {

    private final MaintenanceFacade maintenanceFacade;

    @Autowired
    public AdminMaintenanceController(MaintenanceFacade maintenanceFacade) {
        this.maintenanceFacade = maintenanceFacade;
    }

    @GetMapping
    public ModelAndView list() {

        ModelAndView modelAndView = new ModelAndView("admin/maintenance-list");

        List<MaintenanceView> all = maintenanceFacade.getAll();
        modelAndView.addObject("requests", all);
        modelAndView.addObject("currentPath", "/admin/maintenance");

        return modelAndView;
    }

    @PostMapping("/{id}/delete")
    public ModelAndView delete(@PathVariable UUID id) {

        maintenanceFacade.delete(id);
        return new ModelAndView("redirect:/admin/maintenance");
    }
}
