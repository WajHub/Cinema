package com.cinema.bookingservice.kafka.event;

import java.util.List;

public record PaymentStartedEventPayload(
    String bookingId,
    String userId,
    String catalogSessionId,
    List<String> catalogSeatIds,
    String amount,
    String totalPrice,
    String createdAt
) {}
