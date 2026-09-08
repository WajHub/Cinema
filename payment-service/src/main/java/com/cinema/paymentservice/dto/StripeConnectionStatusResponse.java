package com.cinema.paymentservice.dto;

public record StripeConnectionStatusResponse(
    boolean connected,
    String status,
    String stripeAccountId,
    String businessName,
    String message
) {}
