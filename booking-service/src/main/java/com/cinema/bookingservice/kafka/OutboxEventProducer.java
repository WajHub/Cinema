package com.cinema.bookingservice.kafka;

import com.cinema.bookingservice.entity.OutboxEventEntity;

public interface OutboxEventProducer {

  boolean supports(String eventType);

  void publish(OutboxEventEntity event);
}
