package com.cinema.paymentservice.kafka.event;

import java.util.UUID;

public record PaymentCompletedEventPayload(
    Long paymentId,
    UUID bookingId,
    String totalPrice,
    String currency,
    String stripeCheckoutSessionId,
    String completedAt,
    String status) {}
