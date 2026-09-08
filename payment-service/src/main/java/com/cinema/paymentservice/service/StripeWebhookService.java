package com.cinema.paymentservice.service;

import com.cinema.kafka.event.PaymentCancelledEventPayload;
import com.cinema.kafka.event.PaymentCompletedEventPayload;
import com.cinema.paymentservice.entity.OutboxEventEntity;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.PaymentStatusHistoryEntity;
import com.cinema.paymentservice.entity.StripeWebhookEventEntity;
import com.cinema.paymentservice.repository.OutboxEventRepository;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.cinema.paymentservice.repository.StripeWebhookEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookService {

  private static final String EVENT_COMPLETED = "checkout.session.completed";
  private static final String EVENT_EXPIRED = "checkout.session.expired";
  private static final String EVENT_FAILED = "payment_intent.payment_failed";

  private final StripeWebhookEventRepository webhookEventRepository;
  private final PaymentRepository paymentRepository;
  private final PaymentStatusHistoryRepository statusHistoryRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  @Value("${stripe.webhook.secret:}")
  private String webhookSecret;

  @Transactional
  public void processWebhook(String payload, String sigHeader) {
    Event event = parseAndVerifyEvent(payload, sigHeader);

    if (webhookEventRepository.existsByStripeEventId(event.getId())) {
      log.info("Webhook event {} already processed, skipping.", event.getId());
      return;
    }

    StripeWebhookEventEntity webhookEntity = new StripeWebhookEventEntity();
    webhookEntity.setStripeEventId(event.getId());
    webhookEventRepository.save(webhookEntity);

    switch (event.getType()) {
      case EVENT_COMPLETED -> handleCheckoutSessionCompleted(event);
      case EVENT_EXPIRED, EVENT_FAILED -> handleCheckoutSessionCancelled(event);
      default -> log.debug("Unhandled Stripe webhook event type: {}", event.getType());
    }
  }

  private Event parseAndVerifyEvent(String payload, String sigHeader) {
    if (webhookSecret != null && !webhookSecret.isBlank()) {
      if (sigHeader == null || sigHeader.isBlank()) {
        throw new IllegalArgumentException("Missing Stripe-Signature header");
      }
      try {
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
      } catch (SignatureVerificationException e) {
        log.error("Invalid Stripe signature", e);
        throw new IllegalArgumentException("Invalid Stripe signature", e);
      }
    }
    try {
      return Event.GSON.fromJson(payload, Event.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse Stripe webhook payload JSON", e);
    }
  }

  private void handleCheckoutSessionCompleted(Event event) {
    Session session = extractSession(event);
    if (session == null) return;

    paymentRepository.findByStripeCheckoutSessionId(session.getId())
        .ifPresent(payment -> {
          if (payment.getCurrentStatus() == PaymentStatus.IN_PROGRESS) {
            payment.setCurrentStatus(PaymentStatus.COMPLETED);
            if (session.getPaymentIntent() != null) {
              payment.setStripePaymentIntentId(session.getPaymentIntent());
            }
            paymentRepository.save(payment);
            saveStatusHistory(payment, PaymentStatus.COMPLETED);
            persistCompletedOutboxEvent(payment);
            log.info("Payment {} completed successfully via Stripe webhook.", payment.getId());
          }
        });
  }

  private void handleCheckoutSessionCancelled(Event event) {
    Session session = extractSession(event);
    if (session == null) return;

    paymentRepository.findByStripeCheckoutSessionId(session.getId())
        .ifPresent(payment -> {
          if (payment.getCurrentStatus() == PaymentStatus.IN_PROGRESS) {
            payment.setCurrentStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            saveStatusHistory(payment, PaymentStatus.CANCELLED);
            persistCancelledOutboxEvent(payment, "Cancelled or expired via Stripe webhook");
            log.info("Payment {} cancelled via Stripe webhook.", payment.getId());
          }
        });
  }

  private Session extractSession(Event event) {
    EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
    StripeObject stripeObject = null;
    if (deserializer.getObject().isPresent()) {
      stripeObject = deserializer.getObject().get();
    } else {
      try {
        stripeObject = deserializer.deserializeUnsafe();
      } catch (Exception e) {
        log.warn("Failed to deserialize Stripe object using deserializeUnsafe: {}", e.getMessage());
      }
    }

    if (stripeObject instanceof Session session) {
      return session;
    }
    return null;
  }

  private void saveStatusHistory(PaymentEntity payment, PaymentStatus status) {
    PaymentStatusHistoryEntity history = new PaymentStatusHistoryEntity();
    history.setPayment(payment);
    history.setStatus(status);
    statusHistoryRepository.save(history);
  }

  private void persistCompletedOutboxEvent(PaymentEntity payment) {
    try {
      PaymentCompletedEventPayload payload = PaymentCompletedEventPayload.newBuilder()
          .setPaymentId(payment.getId().toString())
          .setBookingId(payment.getBookingId().toString())
          .setTotalPrice(payment.getTotalPrice().toPlainString())
          .setCurrency(payment.getCurrency())
          .setStripeCheckoutSessionId(payment.getStripeCheckoutSessionId())
          .setCompletedAt(OffsetDateTime.now().toString())
          .setStatus("completed")
          .build();

      OutboxEventEntity outbox = new OutboxEventEntity();
      outbox.setAggregateType("payment");
      outbox.setAggregateId(payment.getBookingId());
      outbox.setType("PaymentCompletedEvent");
      outbox.setPayload(objectMapper.writeValueAsString(payload));
      outboxEventRepository.save(outbox);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to persist PaymentCompletedEvent to outbox", e);
    }
  }

  private void persistCancelledOutboxEvent(PaymentEntity payment, String reason) {
    try {
      PaymentCancelledEventPayload payload = PaymentCancelledEventPayload.newBuilder()
          .setPaymentId(payment.getId().toString())
          .setBookingId(payment.getBookingId().toString())
          .setCatalogSessionId(payment.getCatalogSessionId().toString())
          .setCatalogSeatIds(readSeatIds(payment.getCatalogSeatIds()))
          .setTotalPrice(payment.getTotalPrice().toPlainString())
          .setCurrency(payment.getCurrency())
          .setStripeCheckoutSessionId(payment.getStripeCheckoutSessionId())
          .setCancelledAt(OffsetDateTime.now().toString())
          .setReason(reason)
          .setStatus("cancelled")
          .build();

      OutboxEventEntity outbox = new OutboxEventEntity();
      outbox.setAggregateType("payment");
      outbox.setAggregateId(payment.getBookingId());
      outbox.setType("PaymentCancelledEvent");
      outbox.setPayload(objectMapper.writeValueAsString(payload));
      outboxEventRepository.save(outbox);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to persist PaymentCancelledEvent to outbox", e);
    }
  }

  private List<String> readSeatIds(String serializedSeatIds) {
    try {
      List<String> seatIds = new ArrayList<>();
      for (var seatId : objectMapper.readTree(serializedSeatIds)) {
        seatIds.add(seatId.asText());
      }
      return seatIds;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to deserialize seat IDs", e);
    }
  }
}
