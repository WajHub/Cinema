package com.cinema.paymentservice.kafka;

import com.cinema.kafka.event.PaymentCancelledEvent;
import com.cinema.kafka.event.PaymentCompletedEvent;
import com.cinema.paymentservice.entity.OutboxEventEntity;
import com.cinema.paymentservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxProducer {

  private static final int BATCH_SIZE = 50;
  private static final String COMPLETED_EVENT = "PaymentCompletedEvent";
  private static final String CANCELLED_EVENT = "PaymentCancelledEvent";

  private final OutboxEventRepository outboxEventRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.kafka.topics.payment-completed}")
  private String paymentCompletedTopic;

  @Value("${app.kafka.topics.payment-cancelled}")
  private String paymentCancelledTopic;

  @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:2000}")
  @Transactional
  public void publishPendingEvents() {
    List<OutboxEventEntity> events = outboxEventRepository.lockNextBatch(BATCH_SIZE);
    for (OutboxEventEntity event : events) {
      try {
        String topic;
        Object kafkaEvent;
        if (COMPLETED_EVENT.equals(event.getType())) {
          topic = paymentCompletedTopic;
          kafkaEvent = toCompletedEvent(event);
        } else if (CANCELLED_EVENT.equals(event.getType())) {
          topic = paymentCancelledTopic;
          kafkaEvent = toCancelledEvent(event);
        } else {
          throw new IllegalStateException("Unsupported payment outbox event type: " + event.getType());
        }

        kafkaTemplate.send(topic, event.getAggregateId()
            .toString(), kafkaEvent);
        outboxEventRepository.deleteById(event.getId());
      } catch (Exception exception) {
        log.error("Failed to publish payment outbox event {}", event.getId(), exception);
        throw new IllegalStateException("Failed to publish payment outbox event", exception);
      }
    }
  }

  private PaymentCompletedEvent toCompletedEvent(OutboxEventEntity event) throws Exception {
    JsonNode root = objectMapper.readTree(event.getPayload());
    return PaymentCompletedEvent.newBuilder()
        .setPaymentId(root.path("paymentId")
            .asText())
        .setBookingId(root.path("bookingId")
            .asText())
        .setTotalPrice(root.path("totalPrice")
            .asText())
        .setCurrency(root.path("currency")
            .asText())
        .setStripeCheckoutSessionId(root.path("stripeCheckoutSessionId")
            .asText())
        .setCompletedAt(root.path("completedAt")
            .asText())
        .setStatus(root.path("status")
            .asText())
        .build();
  }

  private PaymentCancelledEvent toCancelledEvent(OutboxEventEntity event) throws Exception {
    JsonNode root = objectMapper.readTree(event.getPayload());
    List<String> catalogSeatIds = new ArrayList<>();
    for (JsonNode seatId : root.path("catalogSeatIds")) {
      catalogSeatIds.add(seatId.asText());
    }

    return PaymentCancelledEvent.newBuilder()
        .setPaymentId(root.path("paymentId")
            .asText())
        .setBookingId(root.path("bookingId")
            .asText())
        .setCatalogSessionId(root.path("catalogSessionId")
            .asText())
        .setCatalogSeatIds(catalogSeatIds)
        .setTotalPrice(root.path("totalPrice")
            .asText())
        .setCurrency(root.path("currency")
            .asText())
        .setStripeCheckoutSessionId(root.path("stripeCheckoutSessionId")
            .asText())
        .setCancelledAt(root.path("cancelledAt")
            .asText())
        .setReason(root.path("reason")
            .asText())
        .setStatus(root.path("status")
            .asText())
        .build();
  }
}
