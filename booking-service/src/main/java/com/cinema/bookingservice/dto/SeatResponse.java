package com.cinema.bookingservice.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SeatResponse(UUID sessionSeatId, UUID catalogSeatId, String rowLabel, Integer seatNumber, BigDecimal finalPrice, String status) {
}
