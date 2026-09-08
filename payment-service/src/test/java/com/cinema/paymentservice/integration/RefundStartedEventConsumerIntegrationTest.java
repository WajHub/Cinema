package com.cinema.paymentservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.cinema.kafka.event.RefundStartedEvent;
import com.cinema.kafka.event.RefundStartedEventPayload;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.RefundEntity;

import com.cinema.paymentservice.kafka.RefundStartedEventConsumer;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;

class RefundStartedEventConsumerIntegrationTest extends IntegrationTestConfiguration {

  @Autowired
  private RefundStartedEventConsumer refundStartedEventConsumer;

  @Test
  @DisplayName("Should process RefundStartedEvent and persist refund entity and outbox event")
  void consumesRefundStartedEventAndUpdatesDatabase() {
    UUID bookingId = UUID.randomUUID();
    UUID paymentId = UUID.randomUUID();

    PaymentEntity payment = new PaymentEntity();
    payment.setBookingId(bookingId);
    payment.setCatalogSessionId(UUID.randomUUID());
    payment.setCatalogSeatIds("[\"" + UUID.randomUUID() + "\"]");
    payment.setTotalPrice(new BigDecimal("45.00"));
    payment.setCurrency("PLN");
    payment.setCurrentStatus(PaymentStatus.COMPLETED);
    payment.setStripeCheckoutSessionId("cs_integration_test123");
    payment.setStripePaymentIntentId("pi_integration_test123");
    payment.setStartedAt(OffsetDateTime.now());
    payment = paymentRepository.save(payment);

    Refund mockStripeRefund = mock(Refund.class);
    when(mockStripeRefund.getId()).thenReturn("re_integration_mock123");

    try (MockedStatic<Refund> mockedStatic = mockStatic(Refund.class)) {
      mockedStatic.when(() -> Refund.create(any(RefundCreateParams.class))).thenReturn(mockStripeRefund);

      RefundStartedEventPayload payload = RefundStartedEventPayload.newBuilder()
          .setBookingId(bookingId.toString())
          .setUserId(UUID.randomUUID().toString())
          .setTotalPrice("45.00")
          .setCurrency("PLN")
          .setCreatedAt("2026-09-08T10:00:00Z")
          .build();

      RefundStartedEvent event = RefundStartedEvent.newBuilder()
          .setPayload(payload)
          .build();

      refundStartedEventConsumer.consume(event);
    }

    PaymentEntity updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
    assertThat(updatedPayment.getCurrentStatus()).isEqualTo(PaymentStatus.REFUNDED);

    List<RefundEntity> refunds = refundRepository.findAll();
    assertThat(refunds).hasSize(1);
    assertThat(refunds.get(0).getStripeRefundId()).isEqualTo("re_integration_mock123");

    assertThat(outboxEventRepository.findAll()).isNotEmpty();
  }
}
