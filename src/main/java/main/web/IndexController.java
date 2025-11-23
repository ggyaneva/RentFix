package main.web;

import jakarta.validation.Valid;
import main.service.UserService;
import main.web.dto.LoginRequest;
import main.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IndexController {

    private final UserService userService;

    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/auth/login")
    public ModelAndView loginPage() {

        ModelAndView modelAndView = new ModelAndView("auth/login");

        modelAndView.addObject("loginRequest", new LoginRequest());
        return modelAndView;
    }

    @GetMapping("/auth/register")
    public ModelAndView registerPage() {

        ModelAndView modelAndView = new ModelAndView("auth/register");

        modelAndView.addObject("registerRequest", new RegisterRequest());
        return modelAndView;
    }


    @PostMapping("/auth/register")
    public ModelAndView register(@Valid RegisterRequest registerRequest,  BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("auth/register");
        }

        userService.register(registerRequest);
        redirectAttributes.addFlashAttribute("successfulRegistration", "You have registered successfully");

        return new ModelAndView("redirect:auth/login");
    }

//    @GetMapping("/redirect-by-role")
//    public String redirectByRole(@AuthenticationPrincipal UserData user) {
//
//        return switch (user.getRole()) {
//            case ADMIN -> "redirect:/admin/dashboard";
//            case OWNER -> "redirect:/owner/dashboard";
//            case TENANT -> "redirect:/tenant/dashboard";
//            default -> "redirect:/";
//        };
//    }


}
