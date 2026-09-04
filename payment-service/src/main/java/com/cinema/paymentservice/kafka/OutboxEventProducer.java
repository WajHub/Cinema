package com.cinema.paymentservice.kafka;

import com.cinema.paymentservice.entity.OutboxEventEntity;

public interface OutboxEventProducer {

  boolean supports(String eventType);

  void publish(OutboxEventEntity event);
}
