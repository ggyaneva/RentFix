package app.web;

import app.model.User;
import app.security.UserData;
import app.service.UserService;
import app.web.dto.EmailChangeRequest;
import app.web.dto.ProfileUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView profile(@AuthenticationPrincipal UserData userData) {

        User user = userService.getById(userData.getUserId());
        ModelAndView modelAndView = new ModelAndView("profile/profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentPath", "/profile");
        modelAndView.addObject("emailChangeRequest", new EmailChangeRequest());

        return modelAndView;
    }


    @PostMapping
    public String update(@ModelAttribute ProfileUpdateRequest request,
                         @AuthenticationPrincipal UserData user) {

        userService.updateProfile(user.getUserId(), request);
        return "redirect:/profile";
    }

    @PostMapping("/email")
    public ModelAndView changeEmail(@AuthenticationPrincipal UserData user,
                              @ModelAttribute EmailChangeRequest request) {

        try {
            userService.changeEmail(user.getUserId(), request.getNewEmail());
            return new ModelAndView("redirect:/profile");
        } catch (Exception e) {
            ModelAndView modelAndView = new ModelAndView("profile/profile");
            modelAndView.addObject("error", e.getMessage());
            return modelAndView;
        }
    }

}

