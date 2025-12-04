package app.service;

import app.exception.ResourceNotFoundException;
import app.model.Payment;
import app.model.RentalContract;
import app.model.User;
import app.model.enums.PaymentStatus;
import app.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void pay_throwsResourceNotFound_whenPaymentMissing() {
        UUID paymentId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.pay(paymentId, tenantId));

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void pay_throwsSecurityException_whenTenantIsNotOwnerOfPayment() {
        UUID paymentId = UUID.randomUUID();
        UUID realTenantId = UUID.randomUUID();
        UUID otherTenantId = UUID.randomUUID();

        User tenant = new User();
        tenant.setId(realTenantId);

        RentalContract contract = new RentalContract();
        contract.setTenant(tenant);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setContract(contract);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(SecurityException.class,
                () -> paymentService.pay(paymentId, otherTenantId));

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void pay_marksPaymentAsSuccess_whenValidTenantAndPending() {
        UUID paymentId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        User tenant = new User();
        tenant.setId(tenantId);

        RentalContract contract = new RentalContract();
        contract.setTenant(tenant);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setContract(contract);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        paymentService.pay(paymentId, tenantId);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());

        Payment saved = captor.getValue();
        assertEquals(PaymentStatus.SUCCESS, saved.getStatus());
        assertNotNull(saved.getPaidAt(), "paidAt should be set when payment is successful");
    }
}

