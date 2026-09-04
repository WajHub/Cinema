package com.cinema.bookingservice.kafka;

import com.cinema.bookingservice.entity.OutboxEventEntity;
import com.cinema.bookingservice.repository.OutboxEventRepository;
import com.cinema.kafka.event.RefundStartedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class RefundStartedProducer {

  private static final int BATCH_SIZE = 50;

  private final OutboxEventRepository outboxEventRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.kafka.topics.refund-started}")
  private String refundStartedTopic;

  @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:2000}")
  @Transactional
  public void publishPendingEvents() {
    List<OutboxEventEntity> events = outboxEventRepository.lockNextBatch(BATCH_SIZE);

    for (OutboxEventEntity event : events) {
      try {
        RefundStartedEvent kafkaEvent = toKafkaEvent(event);
        kafkaTemplate.send(refundStartedTopic, event.getAggregateId()
            .toString(), kafkaEvent);
        outboxEventRepository.deleteById(event.getId());
        log.info("Published RefundStartedEvent for booking: {}", event.getAggregateId());
      } catch (Exception exception) {
        log.error("Failed to publish RefundStartedEvent for booking: {}", event.getAggregateId(), exception);
        throw new IllegalStateException("Failed to publish event", exception);
      }
    }
  }

  private RefundStartedEvent toKafkaEvent(OutboxEventEntity event) throws Exception {
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
  }
}
