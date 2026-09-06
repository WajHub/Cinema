package com.cinema.bookingservice.kafka.event;

import java.util.List;
import java.util.UUID;

public record PaymentStartedEventPayload(
    UUID bookingId,
    UUID userId,
    UUID catalogSessionId,
    List<UUID> catalogSeatIds,
    String totalPrice,
    String createdAt) {}
