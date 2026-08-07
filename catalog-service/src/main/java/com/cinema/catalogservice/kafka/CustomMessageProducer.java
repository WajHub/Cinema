package com.cinema.catalogservice.kafka;

import com.cinema.kafka.event.CatalogEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomMessageProducer {
  private final KafkaTemplate<String, CatalogEvent> kafkaTemplate;
  private final String topic;

  @Autowired
  public CustomMessageProducer(KafkaTemplate<String, CatalogEvent> kafkaTemplate, @Value("${app.kafka.topics.catalog-events}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public void sendMessage(CatalogEvent message) {
    kafkaTemplate.send(topic, message);
  }
}
