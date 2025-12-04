package app.web;

import app.model.Property;
import app.model.enums.Role;
import app.repository.RentalContractRepository;
import app.security.UserData;
import app.service.PropertyService;
import app.web.dto.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final RentalContractRepository rentalContractRepository;

    @Autowired
    public PropertyController(PropertyService propertyService, RentalContractRepository rentalContractRepository) {
        this.propertyService = propertyService;
        this.rentalContractRepository = rentalContractRepository;
    }

    @GetMapping
    public ModelAndView search(@RequestParam(required = false) String city,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Integer minBedrooms,
                         @RequestParam(required = false) Integer maxBedrooms,
                         @AuthenticationPrincipal UserData user) {

        ModelAndView modelAndView = new ModelAndView("properties/list");
        modelAndView.addObject("currentPath", "/properties");

        if (user.getRole() == Role.TENANT) {
            modelAndView.addObject("properties",
                    propertyService.searchAvailable(city, keyword, minBedrooms, maxBedrooms));
            return modelAndView;
        }

        modelAndView.addObject("properties",
                propertyService.search(city, keyword, minBedrooms, maxBedrooms));

        return modelAndView;
    }


    @GetMapping("/{id}")
    public ModelAndView details(@PathVariable UUID id,
                          @AuthenticationPrincipal UserData user) {

        Property property = propertyService.getById(id);
        ModelAndView modelAndView = new ModelAndView("properties/details");
        modelAndView.addObject("property", property);
        modelAndView.addObject("currentPath", "/properties");

        boolean hasActive = rentalContractRepository
                .existsByTenantIdAndActiveTrue(user.getUserId());

        modelAndView.addObject("hasActiveContract", hasActive);

        return modelAndView;
    }


    @GetMapping("/owner/create")
    public ModelAndView createPage() {
        ModelAndView modelAndView = new ModelAndView("owner/property-create");

        modelAndView.addObject("propertyRequest", new PropertyRequest());
        modelAndView.addObject("currentPath", "/owner/dashboard");

        return modelAndView;
    }

    @PostMapping("/owner/create")
    public String create(@ModelAttribute PropertyRequest request,
                         @AuthenticationPrincipal UserData user) {

        propertyService.create(request, user.getUserId());
        return "redirect:/owner/dashboard";
    }

    @GetMapping("/owner/edit/{id}")
    public ModelAndView editPage(@PathVariable UUID id) {
        ModelAndView modelAndView = new ModelAndView("owner/property-edit");
        modelAndView.addObject("property", propertyService.getById(id));
        modelAndView.addObject("currentPath", "/owner/dashboard");

        return modelAndView;
    }

    @PostMapping("/owner/edit/{id}")
    public String edit(@PathVariable UUID id,
                       @ModelAttribute PropertyRequest request,
                       @AuthenticationPrincipal UserData user) {

        propertyService.update(id, request, user.getUserId());
        return "redirect:/owner/dashboard";
    }

    @PostMapping("/owner/delete/{id}")
    public String delete(@PathVariable UUID id,
                         @AuthenticationPrincipal UserData user) {

        propertyService.delete(id, user.getUserId());
        return "redirect:/owner/dashboard";
    }
}
