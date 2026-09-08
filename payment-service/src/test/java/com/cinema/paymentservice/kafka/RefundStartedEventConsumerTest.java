package com.cinema.paymentservice.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cinema.kafka.event.RefundStartedEvent;
import com.cinema.kafka.event.RefundStartedEventPayload;
import com.cinema.paymentservice.config.JacksonConfig;
import com.cinema.paymentservice.entity.OutboxEventEntity;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.RefundEntity;
import com.cinema.paymentservice.entity.RefundStatus;
import com.cinema.paymentservice.repository.OutboxEventRepository;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.cinema.paymentservice.repository.RefundRepository;
import com.cinema.paymentservice.service.StripeRefundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Refund;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefundStartedEventConsumerTest {

  @Mock
  private RefundRepository refundRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private PaymentStatusHistoryRepository statusHistoryRepository;

  @Mock
  private OutboxEventRepository outboxEventRepository;

  @Mock
  private StripeRefundService stripeRefundService;

  @Spy
  private ObjectMapper objectMapper = new JacksonConfig().objectMapper();

  @InjectMocks
  private RefundStartedEventConsumer consumer;

  @Test
  void consume_processesStripeRefundAndUpdatePaymentStatus() {
    UUID bookingId = UUID.randomUUID();
    UUID paymentId = UUID.randomUUID();

    PaymentEntity payment = new PaymentEntity();
    payment.setId(paymentId);
    payment.setBookingId(bookingId);
    payment.setCatalogSessionId(UUID.randomUUID());
    payment.setCatalogSeatIds("[\"" + UUID.randomUUID() + "\"]");
    payment.setTotalPrice(new BigDecimal("45.00"));
    payment.setCurrency("PLN");
    payment.setCurrentStatus(PaymentStatus.COMPLETED);
    payment.setStripeCheckoutSessionId("cs_test_123");
    payment.setStripePaymentIntentId("pi_test_123");

    when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));

    Refund mockStripeRefund = mock(Refund.class);
    when(mockStripeRefund.getId()).thenReturn("re_test_mock123");
    when(stripeRefundService.processRefund(payment)).thenReturn(mockStripeRefund);

    when(refundRepository.save(any(RefundEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

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

    consumer.consume(event);

    assertThat(payment.getCurrentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    verify(stripeRefundService).processRefund(payment);
    verify(refundRepository, org.mockito.Mockito.times(2)).save(any(RefundEntity.class));
    verify(outboxEventRepository).save(any(OutboxEventEntity.class));
  }
}
