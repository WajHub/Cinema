package com.cinema.paymentservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreatePaymentRequest(
    @NotNull(message = "Booking ID is required")
    UUID bookingId,
    @NotNull(message = "Catalog session ID is required")
    UUID catalogSessionId,
    @NotEmpty(message = "At least one seat ID is required")
    List<UUID> catalogSeatIds,
    @NotNull(message = "Total price is required")
    BigDecimal totalPrice
) {}
