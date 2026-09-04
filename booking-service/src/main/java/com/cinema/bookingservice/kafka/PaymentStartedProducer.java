package com.cinema.bookingservice.kafka;

import com.cinema.bookingservice.entity.OutboxEventEntity;
import com.cinema.kafka.event.PaymentStartedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStartedProducer implements OutboxEventProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.kafka.topics.payment-started}")
  private String paymentStartedTopic;

  @Override
  public boolean supports(String eventType) {
    return "PaymentStartedEvent".equals(eventType);
  }

  @Override
  public void publish(OutboxEventEntity event) {
    kafkaTemplate.send(paymentStartedTopic, event.getAggregateId()
        .toString(), toKafkaEvent(event));
  }

  private PaymentStartedEvent toKafkaEvent(OutboxEventEntity event) {
    try {
      JsonNode root = objectMapper.readTree(event.getPayload());

      List<String> catalogSeatIds = new ArrayList<>();
      for (JsonNode seatNode : root.path("catalogSeatIds")) {
        catalogSeatIds.add(seatNode.asText());
      }

      return PaymentStartedEvent.newBuilder()
          .setBookingId(root.path("bookingId")
              .asText())
          .setUserId(root.path("userId")
              .asText())
          .setCatalogSessionId(root.path("catalogSessionId")
              .asText())
          .setCatalogSeatIds(catalogSeatIds)
          .setTotalPrice(root.path("totalPrice")
              .asText())
          .setCreatedAt(root.path("createdAt")
              .asText())
          .build();
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to convert PaymentStartedEvent outbox payload", exception);
    }
  }
}
