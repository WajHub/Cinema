package com.cinema.catalogservice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SessionRequest(
    @NotNull UUID auditoryId,
    @NotNull UUID movieId,
    @NotNull OffsetDateTime startsAt,
    @NotNull OffsetDateTime endsAt,
    @NotNull @Positive BigDecimal basePrice,
    @NotBlank String status) {
}