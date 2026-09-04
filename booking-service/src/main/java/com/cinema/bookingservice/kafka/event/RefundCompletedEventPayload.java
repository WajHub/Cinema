package com.cinema.paymentservice.kafka.event;

import java.util.UUID;
import lombok.Builder;

@Builder
public record RefundCompletedEventPayload( //
    UUID bookingId, //
    UUID userId, //
    String totalPrice, //
    String createdAt //
) {
}
