package com.cinema.bookingservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SeatReservationResponse(
    UUID catalogSeatId,
    String rowLabel,
    Integer seatNumber,
    BigDecimal finalPrice,
    String status,
    UUID bookingId
) {}
