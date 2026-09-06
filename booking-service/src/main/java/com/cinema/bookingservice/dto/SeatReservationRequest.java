package com.cinema.bookingservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record SeatReservationRequest(//
    @NotNull(message = "User ID is required") //
    UUID userId, //
    @NotEmpty(message = "At least one seat ID is required") //
    List<UUID> catalogSeatIds //
) {
}
