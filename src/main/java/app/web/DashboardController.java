package app.web;

import app.security.UserData;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserData user) {
        return switch (user.getRole()) {
            case ADMIN -> "redirect:/admin/dashboard";
            case OWNER -> "redirect:/owner/dashboard";
            case TENANT -> "redirect:/tenant/dashboard";
        };
    }

}

