package com.cinema.paymentservice.kafka.event;

import java.util.List;
import java.util.UUID;

public record PaymentCancelledEventPayload(
        UUID paymentId,
    UUID bookingId,
    UUID catalogSessionId,
    List<String> catalogSeatIds,
    String totalPrice,
    String currency,
    String stripeCheckoutSessionId,
    String cancelledAt,
    String reason,
    String status) {}
