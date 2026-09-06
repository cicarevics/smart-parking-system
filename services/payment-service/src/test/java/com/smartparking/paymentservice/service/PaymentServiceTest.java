package com.smartparking.paymentservice.service;

import com.smartparking.paymentservice.client.ReservationServiceClient;
import com.smartparking.paymentservice.client.ReservationView;
import com.smartparking.paymentservice.dto.ExtensionCreateRequest;
import com.smartparking.paymentservice.dto.PaymentCreateRequest;
import com.smartparking.paymentservice.exception.ReservationNotPayableException;
import com.smartparking.paymentservice.model.Payment;
import com.smartparking.paymentservice.model.PaymentStatus;
import com.smartparking.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationServiceClient reservationServiceClient;

    private PaymentService service() {
        return new PaymentService(paymentRepository, reservationServiceClient);
    }

    private static ReservationView reservationView(UUID id, String status) {
        ReservationView view = new ReservationView();
        view.setId(id);
        view.setStatus(status);
        return view;
    }

    @Test
    void pay_declinedCard_marksPaymentFailedAndCancelsRatherThanConfirms() {
        PaymentService paymentService = service();
        UUID driverId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        String authHeader = "Bearer driver-token";
        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setReservationId(reservationId);
        request.setAmount(BigDecimal.TEN);
        request.setPaymentMethod("card_declined");

        when(reservationServiceClient.fetch(reservationId, authHeader))
                .thenReturn(reservationView(reservationId, "PENDING_PAYMENT"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.pay(driverId, authHeader, request);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(reservationServiceClient).cancel(reservationId, authHeader);
        verify(reservationServiceClient, never()).confirm(any(), any());
    }

    @Test
    void pay_successfulCharge_confirmsRatherThanCancels() {
        PaymentService paymentService = service();
        UUID driverId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        String authHeader = "Bearer driver-token";
        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setReservationId(reservationId);
        request.setAmount(BigDecimal.TEN);
        request.setPaymentMethod("visa");

        when(reservationServiceClient.fetch(reservationId, authHeader))
                .thenReturn(reservationView(reservationId, "PENDING_PAYMENT"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.pay(driverId, authHeader, request);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        verify(reservationServiceClient).confirm(reservationId, driverId);
        verify(reservationServiceClient, never()).cancel(any(), any());
    }

    @Test
    void pay_reservationNotPendingPayment_throwsBeforeAnyCharge() {
        PaymentService paymentService = service();
        UUID driverId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        String authHeader = "Bearer driver-token";
        PaymentCreateRequest request = new PaymentCreateRequest();
        request.setReservationId(reservationId);
        request.setAmount(BigDecimal.TEN);
        request.setPaymentMethod("visa");

        when(reservationServiceClient.fetch(reservationId, authHeader))
                .thenReturn(reservationView(reservationId, "ACTIVE"));

        assertThatThrownBy(() -> paymentService.pay(driverId, authHeader, request))
                .isInstanceOf(ReservationNotPayableException.class);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void payForExtension_declinedCard_leavesReservationUntouched() {
        PaymentService paymentService = service();
        UUID driverId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        String authHeader = "Bearer driver-token";
        ExtensionCreateRequest request = new ExtensionCreateRequest();
        request.setReservationId(reservationId);
        request.setAdditionalMinutes(30);
        request.setAmount(BigDecimal.TEN);
        request.setPaymentMethod("card_declined");

        when(reservationServiceClient.fetch(reservationId, authHeader))
                .thenReturn(reservationView(reservationId, "ACTIVE"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.payForExtension(driverId, authHeader, request);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(reservationServiceClient).fetch(reservationId, authHeader);
        verifyNoMoreInteractions(reservationServiceClient);
    }

    @Test
    void payForExtension_successfulCharge_extendsReservation() {
        PaymentService paymentService = service();
        UUID driverId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        String authHeader = "Bearer driver-token";
        ExtensionCreateRequest request = new ExtensionCreateRequest();
        request.setReservationId(reservationId);
        request.setAdditionalMinutes(30);
        request.setAmount(BigDecimal.TEN);
        request.setPaymentMethod("visa");

        when(reservationServiceClient.fetch(reservationId, authHeader))
                .thenReturn(reservationView(reservationId, "ACTIVE"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.payForExtension(driverId, authHeader, request);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        verify(reservationServiceClient).extend(reservationId, driverId, 30);
    }
}
