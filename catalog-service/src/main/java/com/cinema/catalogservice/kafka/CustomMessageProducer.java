package com.cinema.catalogservice.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomMessageProducer {
  private final KafkaTemplate<String, CustomMessage> kafkaTemplate;
  private final String topic;

  @Autowired
  public CustomMessageProducer(KafkaTemplate<String, CustomMessage> kafkaTemplate, @Value("${app.kafka.topics.catalog-events}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public void sendMessage(CustomMessage message) {
    kafkaTemplate.send(topic, message);
  }
}
