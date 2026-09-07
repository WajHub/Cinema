package com.cinema.paymentservice.dto;

import java.util.UUID;

public record CreatePaymentResponse(
    UUID paymentId,
    UUID bookingId,
    String stripeCheckoutSessionId,
    String checkoutUrl
) {}
