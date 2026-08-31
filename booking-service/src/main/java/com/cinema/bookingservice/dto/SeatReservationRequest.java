package com.cinema.bookingservice.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record SeatReservationRequest(
    @NotNull(message = "User ID is required")
    UUID userId
) {}
