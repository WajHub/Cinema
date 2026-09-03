package com.cinema.catalogservice.kafka;

import com.cinema.catalogservice.entity.OutboxEventEntity;
import com.cinema.catalogservice.repository.OutboxEventRepository;
import com.cinema.kafka.event.SessionChangedEvent;
import com.cinema.kafka.event.SessionChangedEventSeat;
import com.cinema.kafka.event.SessionChangedEventType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SessionOutboxPublisher {

  private final OutboxEventRepository outboxEventRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String topic;

  @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:2000}")
  @Transactional
  public void publishPendingEvents() {
    List<OutboxEventEntity> events = outboxEventRepository.lockNextBatch(50);
    for (OutboxEventEntity event : events) {
      kafkaTemplate.send(topic, event.getAggregateId().toString(), toKafkaEvent(event));
      outboxEventRepository.deleteById(event.getId());
    }
  }

  private SessionChangedEvent toKafkaEvent(OutboxEventEntity event) {
    try {
      JsonNode root = objectMapper.readTree(event.getPayload());
      List<SessionChangedEventSeat> seats = new ArrayList<>();
      for (JsonNode seatNode : root.path("seats")) {
        seats.add(SessionChangedEventSeat.newBuilder()
            .setSeatId(seatNode.path("seatId").asText())
            .setRowLabel(seatNode.path("rowLabel").asText())
            .setSeatNumber(seatNode.path("seatNumber").asInt())
            .setPrice(seatNode.path("price").asText())
            .build());
      }
      return SessionChangedEvent.newBuilder()
          .setEventType(SessionChangedEventType.valueOf(root.path("eventType").asText()))
          .setSessionId(root.path("sessionId").asText())
          .setAuditoryId(root.path("auditoryId").asText())
          .setAuditoryName(root.path("auditoryName").asText())
          .setMovieId(root.path("movieId").asText())
          .setMovieTitle(root.path("movieTitle").asText())
          .setStartsAt(root.path("startsAt").asText())
          .setEndsAt(root.path("endsAt").asText())
          .setStatus(root.path("status").asText())
          .setBasePrice(root.path("basePrice").asText())
          .setSeats(seats)
          .build();
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to convert outbox payload to Kafka event", exception);
    }
  }

  public SessionOutboxPublisher(OutboxEventRepository outboxEventRepository,
      KafkaTemplate<String, Object> kafkaTemplate,
      ObjectMapper objectMapper,
      @Value("${app.kafka.topics.catalog-events}") String topic) {
    this.outboxEventRepository = outboxEventRepository;
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.topic = topic;
  }
}
