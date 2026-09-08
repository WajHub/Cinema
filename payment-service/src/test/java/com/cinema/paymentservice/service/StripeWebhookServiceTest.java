package com.cinema.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cinema.paymentservice.config.JacksonConfig;
import com.cinema.paymentservice.entity.OutboxEventEntity;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.StripeWebhookEventEntity;
import com.cinema.paymentservice.repository.OutboxEventRepository;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.cinema.paymentservice.repository.StripeWebhookEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class StripeWebhookServiceTest {

  @Mock
  private StripeWebhookEventRepository webhookEventRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private PaymentStatusHistoryRepository statusHistoryRepository;

  @Mock
  private OutboxEventRepository outboxEventRepository;

  @Spy
  private ObjectMapper objectMapper = new JacksonConfig().objectMapper();

  @InjectMocks
  private StripeWebhookService webhookService;

  @Test
  void processWebhook_skipsAlreadyProcessedEvent() {
    String eventId = "evt_test123";
    String payload = "{\"id\":\"" + eventId + "\",\"type\":\"checkout.session.completed\"}";

    when(webhookEventRepository.existsByStripeEventId(eventId)).thenReturn(true);

    webhookService.processWebhook(payload, null);

    verify(webhookEventRepository, never()).save(any());
    verify(paymentRepository, never()).save(any());
  }

  @Test
  void processWebhook_checkoutSessionCompleted_updatesPaymentStatusToCompleted() {
    String eventId = "evt_test456";
    String sessionId = "cs_test_session789";
    String payload = """
        {
          "id": "%s",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "object": "checkout.session",
              "id": "%s",
              "payment_intent": "pi_test_intent999"
            }
          }
        }
        """.formatted(eventId, sessionId);

    PaymentEntity payment = new PaymentEntity();
    payment.setId(UUID.randomUUID());
    payment.setBookingId(UUID.randomUUID());
    payment.setStripeCheckoutSessionId(sessionId);
    payment.setTotalPrice(new BigDecimal("45.00"));
    payment.setCurrency("PLN");
    payment.setCurrentStatus(PaymentStatus.IN_PROGRESS);

    when(webhookEventRepository.existsByStripeEventId(eventId)).thenReturn(false);
    when(paymentRepository.findByStripeCheckoutSessionId(sessionId)).thenReturn(Optional.of(payment));

    webhookService.processWebhook(payload, null);

    assertThat(payment.getCurrentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(payment.getStripePaymentIntentId()).isEqualTo("pi_test_intent999");

    verify(webhookEventRepository).save(any(StripeWebhookEventEntity.class));
    verify(paymentRepository).save(payment);
    verify(statusHistoryRepository).save(any());
    verify(outboxEventRepository).save(any(OutboxEventEntity.class));
  }
}
