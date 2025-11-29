package app.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import app.model.Payment;
import app.model.RentalContract;
import app.model.enums.PaymentStatus;
import app.model.enums.PaymentType;
import app.repository.PaymentRepository;
import app.repository.RentalContractRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final RentalContractRepository rentalContractRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Runs every day at 06:00
     * Generates ONE monthly rent payment for next month.
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void generateMonthlyPayments() {

        log.info("[Scheduler] Starting monthly payment generation...");

        List<RentalContract> activeContracts =
                rentalContractRepository.findByActiveTrue();

        for (RentalContract contract : activeContracts) {

            LocalDate nextDue = LocalDate.now()
                    .withDayOfMonth(1)
                    .plusMonths(1);

            boolean exists = paymentRepository
                    .existsByContractIdAndTypeAndDueDate(
                            contract.getId(),
                            PaymentType.MONTHLY_RENT,
                            nextDue
                    );

            if (exists) {
                log.info("[Scheduler] Monthly payment already exists for contract {} at {}",
                        contract.getId(), nextDue);
                continue;
            }

            Payment p = new Payment();
            p.setId(UUID.randomUUID());
            p.setContract(contract);
            p.setAmount(contract.getMonthlyRent());
            p.setDueDate(nextDue);
            p.setStatus(PaymentStatus.PENDING);
            p.setType(PaymentType.MONTHLY_RENT);
            p.setPaidAt(null);

            paymentRepository.save(p);

            log.info("[Scheduler] NEW monthly payment created for contract {} due at {}",
                    contract.getId(), nextDue);
        }
    }
}
