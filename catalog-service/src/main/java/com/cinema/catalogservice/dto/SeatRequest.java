package com.cinema.catalogservice.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SeatRequest(
    @NotBlank String rowLabel,
    @NotNull @Positive Integer seatNumber,
    @NotNull @Positive BigDecimal seatPrice,
    @NotBlank String seatType) {
}