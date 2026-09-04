package com.cinema.paymentservice.kafka.event;

import java.util.UUID;

public record PaymentCompletedEventPayload(
        UUID paymentId,
    UUID bookingId,
    String totalPrice,
    String currency,
    String stripeCheckoutSessionId,
    String completedAt,
    String status) {}
