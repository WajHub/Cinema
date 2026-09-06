package com.cinema.catalogservice.kafka;

import com.cinema.catalogservice.entity.OutboxEventEntity;

public interface OutboxEventProducer {

  boolean supports(String eventType);

  void publish(OutboxEventEntity event);
}
