package com.cinema.catalogservice.dto;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AuditoryRequest(
    @NotNull UUID cinemaId,
    @NotBlank String name,
    @NotNull @Positive Integer capacity,
    boolean active,
    @NotEmpty List<@Valid SeatRequest> seats) {
}