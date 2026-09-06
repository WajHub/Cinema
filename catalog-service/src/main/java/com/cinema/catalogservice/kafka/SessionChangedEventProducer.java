package com.cinema.catalogservice.kafka;

import com.cinema.catalogservice.entity.OutboxEventEntity;
import com.cinema.kafka.event.SessionChangedEvent;
import com.cinema.kafka.event.SessionChangedEventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SessionChangedEventProducer implements OutboxEventProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String topic;

  @Override
  public boolean supports(String eventType) {
    return "SessionChangedEvent".equals(eventType);
  }

  @Override
  public void publish(OutboxEventEntity event) {
    kafkaTemplate.send(topic, event.getAggregateId().toString(), toKafkaEvent(event));
  }

  private SessionChangedEvent toKafkaEvent(OutboxEventEntity event) {
    try {
      SessionChangedEventPayload payload = objectMapper.readValue(
          event.getPayload(), SessionChangedEventPayload.class);
      return SessionChangedEvent.newBuilder()
          .setPayload(payload)
          .build();
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to convert outbox payload to Kafka event", exception);
    }
  }

  public SessionChangedEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
      ObjectMapper objectMapper,
      @Value("${app.kafka.topics.catalog-events}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.topic = topic;
  }
}
