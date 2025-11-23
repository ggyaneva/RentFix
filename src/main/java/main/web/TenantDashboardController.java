    package main.web;

    import main.model.Payment;
    import main.model.RentalContract;
    import main.security.UserData;
    import main.service.ContractService;
    import main.service.PaymentService;
    import main.service.UserService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.jackson.JsonMixinModuleEntries;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.servlet.ModelAndView;

    import java.util.List;
    import java.util.UUID;

    @Controller
    @RequestMapping("/tenant")
    public class TenantDashboardController {

        private final ContractService contractService;
        private final PaymentService paymentService;
        private final ContractService rentalContractService;
        private final UserService userService;
        private final JsonMixinModuleEntries jsonMixinModuleEntries;

        @Autowired
        public TenantDashboardController(ContractService contractService, PaymentService paymentService, ContractService rentalContractService, UserService userService, JsonMixinModuleEntries jsonMixinModuleEntries) {
            this.contractService = contractService;
            this.paymentService = paymentService;
            this.rentalContractService = rentalContractService;
            this.userService = userService;
            this.jsonMixinModuleEntries = jsonMixinModuleEntries;
        }

        @GetMapping("/dashboard")
        public ModelAndView dashboard(@AuthenticationPrincipal UserData user) {

            RentalContract active = contractService.getActiveContract(user.getUserId());
            ModelAndView modelAndView = new ModelAndView("tenant/dashboard");
            modelAndView.addObject("activeContract", active);

            modelAndView.addObject("contracts",
                    contractService.getHistoryForTenant(user.getUserId()));

            modelAndView.addObject("currentPath", "/tenant/dashboard");

            return modelAndView;
        }

        @GetMapping("/payments")
        public ModelAndView list(@AuthenticationPrincipal UserData user) {

            List<Payment> payments = paymentService.getPaymentsForTenant(user.getUserId());
            ModelAndView modelAndView = new ModelAndView("tenant/payments");
            modelAndView.addObject("payments", payments);
            modelAndView.addObject("currentPath", "/tenant/payments");

            return modelAndView;
        }

        @PostMapping("/payments/pay/{id}")
        public String pay(@PathVariable UUID id,
                          @AuthenticationPrincipal UserData user) {

            paymentService.pay(id, user.getUserId());
            return "redirect:/tenant/payments";
        }

        @GetMapping("/contract-history")
        public ModelAndView contractHistory(@AuthenticationPrincipal UserData user) {

            UUID tenantId = user.getUserId();

            List<RentalContract> history = rentalContractService.getContractHistoryByTenant(tenantId);

            ModelAndView modelAndView = new ModelAndView("tenant/contract-history");
            modelAndView.addObject("history", history);

            return modelAndView;
        }


    }

