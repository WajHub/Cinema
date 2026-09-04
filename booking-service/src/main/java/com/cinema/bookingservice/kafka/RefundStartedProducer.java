package com.cinema.bookingservice.kafka;

import com.cinema.bookingservice.entity.OutboxEventEntity;
import com.cinema.kafka.event.RefundStartedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundStartedProducer implements OutboxEventProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.kafka.topics.refund-started}")
  private String refundStartedTopic;

  @Override
  public boolean supports(String eventType) {
    return "RefundStartedEvent".equals(eventType);
  }

  @Override
  public void publish(OutboxEventEntity event) {
    kafkaTemplate.send(refundStartedTopic, event.getAggregateId()
        .toString(), toKafkaEvent(event));
  }

  private RefundStartedEvent toKafkaEvent(OutboxEventEntity event) {
    try {
      JsonNode root = objectMapper.readTree(event.getPayload());
      return RefundStartedEvent.newBuilder()
          .setBookingId(root.path("bookingId")
              .asText())
          .setUserId(root.path("userId")
              .asText())
          .setTotalPrice(root.path("totalPrice")
              .asText())
          .setCurrency(root.path("currency")
              .asText())
          .setCreatedAt(root.path("createdAt")
              .asText())
          .build();
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to convert RefundStartedEvent outbox payload", exception);
    }
  }
}
