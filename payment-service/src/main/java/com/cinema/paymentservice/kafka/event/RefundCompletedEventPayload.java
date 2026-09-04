package com.cinema.paymentservice.kafka.event;

import java.util.UUID;
import lombok.Builder;

@Builder
public record RefundCompletedEventPayload( //
    UUID bookingId, //
    String totalPrice, //
    String createdAt //
) {
}
