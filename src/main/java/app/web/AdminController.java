package app.web;


import app.model.enums.PaymentStatus;
import app.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView dashboard() {

        ModelAndView modelAndView = new ModelAndView("admin/dashboard");

        modelAndView.addObject("users", adminService.getAllUsers());
        modelAndView.addObject("properties", adminService.getAllProperties());
        modelAndView.addObject("contracts", adminService.getAllContracts());
        modelAndView.addObject("payments", adminService.getAllPayments());
        modelAndView.addObject("maintenanceList", adminService.getAllMaintenance());

        modelAndView.addObject("currentPath", "/admin/dashboard");

        return modelAndView;
    }


    @GetMapping("/properties")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView allProperties() {

        ModelAndView modelAndView = new ModelAndView("admin/properties");

        modelAndView.addObject("properties", adminService.getAllProperties());
        modelAndView.addObject("currentPath", "/admin/properties");

        return modelAndView;
    }

    @PostMapping("/property/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProperty(@PathVariable UUID id) {
        adminService.deleteProperty(id);
        return "redirect:/admin/properties";
    }


    @GetMapping("/contracts")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView allContracts() {

        ModelAndView modelAndView = new ModelAndView("admin/contracts");

        modelAndView.addObject("contracts", adminService.getAllContracts());
        modelAndView.addObject("currentPath", "/admin/contracts");

        return modelAndView;
    }

    @PostMapping("/contract/end/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String endContract(@PathVariable UUID id) {
        adminService.endContract(id);
        return "redirect:/admin/contracts";
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView allPayments() {

        ModelAndView modelAndView = new ModelAndView("admin/payments");

        modelAndView.addObject("payments", adminService.getAllPayments());
        modelAndView.addObject("currentPath", "/admin/payments");

        return modelAndView;
    }

    @PostMapping("/payment/correct")
    @PreAuthorize("hasRole('ADMIN')")
    public String correctPayment(@RequestParam UUID id,
                                 @RequestParam BigDecimal amount,
                                 @RequestParam PaymentStatus status) {

        adminService.correctPayment(id, amount, status);
        return "redirect:/admin/payments";
    }


    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView manageUsers() {

        ModelAndView modelAndView = new ModelAndView("admin/users");

        modelAndView.addObject("users", adminService.getAllUsers());
        modelAndView.addObject("currentPath", "/admin/users");

        return modelAndView;
    }

    @PostMapping("/users/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return "redirect:/admin/users";
    }
}
