package com.cinema.paymentservice.kafka.event;

import java.util.UUID;
import lombok.Builder;

@Builder
public record RefundStartedEventPayload( //
    UUID bookingId, //
    UUID userId, //
    String totalPrice, //
    String currency, //
    String createdAt //
) {
}
