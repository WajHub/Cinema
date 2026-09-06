package com.cinema.paymentservice.kafka;

import com.cinema.kafka.event.PaymentCancelledEvent;
import com.cinema.kafka.event.PaymentCancelledEventPayload;
import com.cinema.kafka.event.PaymentCompletedEvent;
import com.cinema.kafka.event.PaymentCompletedEventPayload;
import com.cinema.kafka.event.RefundCompletedEvent;
import com.cinema.kafka.event.RefundCompletedEventPayload;
import com.cinema.paymentservice.entity.OutboxEventEntity;
import com.cinema.paymentservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxProducer implements OutboxEventProducer {
  private static final String COMPLETED_EVENT = "PaymentCompletedEvent";
  private static final String CANCELLED_EVENT = "PaymentCancelledEvent";
  private static final String REFUND_COMPLETED_EVENT = "RefundCompletedEvent";

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.kafka.topics.payment-completed}")
  private String paymentCompletedTopic;

  @Value("${app.kafka.topics.payment-cancelled}")
  private String paymentCancelledTopic;

  @Value("${app.kafka.topics.refund-completed}")
  private String refundCompletedTopic;

  @Override
  public boolean supports(String eventType) {
      return COMPLETED_EVENT.equals(eventType) || CANCELLED_EVENT.equals(eventType)
              || REFUND_COMPLETED_EVENT.equals(eventType);
  }

  @Override
  public void publish(OutboxEventEntity event) {
      try {
          String topic;
          Object kafkaEvent;
          if (COMPLETED_EVENT.equals(event.getType())) {
              topic = paymentCompletedTopic;
              kafkaEvent = toCompletedEvent(event);
          } else if (CANCELLED_EVENT.equals(event.getType())) {
              topic = paymentCancelledTopic;
              kafkaEvent = toCancelledEvent(event);
          } else if (REFUND_COMPLETED_EVENT.equals(event.getType())) {
              topic = refundCompletedTopic;
              kafkaEvent = toRefundCompletedEvent(event);
          } else {
              throw new IllegalStateException("Unsupported payment outbox event type: " + event.getType());
          }
          kafkaTemplate.send(topic, event.getAggregateId().toString(), kafkaEvent);
      } catch (Exception exception) {
          log.error("Failed to publish payment outbox event {}", event.getId(), exception);
          throw new IllegalStateException("Failed to publish payment outbox event", exception);
      }
  }

  private PaymentCompletedEvent toCompletedEvent(OutboxEventEntity event) throws Exception {
    PaymentCompletedEventPayload payload = objectMapper.readValue(
        event.getPayload(), PaymentCompletedEventPayload.class);
    return PaymentCompletedEvent.newBuilder()
        .setPayload(payload)
        .build();
  }

  private PaymentCancelledEvent toCancelledEvent(OutboxEventEntity event) throws Exception {
    PaymentCancelledEventPayload payload = objectMapper.readValue(
        event.getPayload(), PaymentCancelledEventPayload.class);
    return PaymentCancelledEvent.newBuilder()
        .setPayload(payload)
        .build();
  }

  private RefundCompletedEvent toRefundCompletedEvent(OutboxEventEntity event) throws Exception {
    RefundCompletedEventPayload payload = objectMapper.readValue(
        event.getPayload(), RefundCompletedEventPayload.class);
    return RefundCompletedEvent.newBuilder()
        .setPayload(payload)
        .build();
  }
}
