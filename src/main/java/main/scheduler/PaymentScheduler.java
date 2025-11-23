package main.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.model.Payment;
import main.model.RentalContract;
import main.model.enums.PaymentStatus;
import main.repository.PaymentRepository;
import main.repository.RentalContractRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final RentalContractRepository rentalContractRepository;
    private final PaymentRepository paymentRepository;


    @Scheduled(cron = "0 0 6 * * ?")
    public void generateMonthlyPayments() {
        log.info("[Scheduler] Checking for new monthly payments...");

        List<RentalContract> activeContracts =
                rentalContractRepository.findByActiveTrue();

        for (RentalContract contract : activeContracts) {

            LocalDate nextDue = LocalDate.now().withDayOfMonth(1).plusMonths(1);

            boolean exists = paymentRepository.existsByContractIdAndDueDate(
                    contract.getId(), nextDue
            );

            if (exists) {
                continue;
            }

            Payment p = new Payment();
            p.setId(UUID.randomUUID());
            p.setContract(contract);
            p.setAmount(contract.getMonthlyRent());
            p.setDueDate(nextDue);
            p.setStatus(PaymentStatus.PENDING);
            p.setPaidAt(null);

            paymentRepository.save(p);

            log.info("[Scheduler] Payment created for contract {} due at {}",
                    contract.getId(), nextDue);
        }
    }
}
