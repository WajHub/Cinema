package com.cinema.catalogservice.kafka.event;

import java.util.List;
import java.util.UUID;

public record SessionChangedEventPayload(
    String eventType,
    UUID sessionId,
    UUID auditoryId,
    String auditoryName,
    UUID movieId,
    String movieTitle,
    String startsAt,
    String endsAt,
    String status,
    String basePrice,
    List<SessionChangedEventSeatPayload> seats) {}
