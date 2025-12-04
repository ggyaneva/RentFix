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

            Payment payment = new Payment();
            payment.setId(UUID.randomUUID());
            payment.setContract(contract);
            payment.setAmount(contract.getMonthlyRent());
            payment.setDueDate(nextDue);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setType(PaymentType.MONTHLY_RENT);
            payment.setPaidAt(null);

            paymentRepository.save(payment);

            log.info("[Scheduler] NEW monthly payment created for contract {} due at {}",
                    contract.getId(), nextDue);
        }
    }


    @Scheduled(fixedDelay = 300_000)  // без cron, изискване №2
    public void checkOverduePayments() {

        log.info("[Scheduler] Checking for overdue payments...");

        LocalDate today = LocalDate.now();

        List<Payment> allPayments = paymentRepository.findAll();

        long overdueCount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .filter(p -> p.getDueDate() != null && p.getDueDate().isBefore(today))
                .count();

        if (overdueCount > 0) {
            log.warn("[Scheduler] Found {} overdue PENDING payments!", overdueCount);
        } else {
            log.info("[Scheduler] No overdue PENDING payments.");
        }
    }
}
