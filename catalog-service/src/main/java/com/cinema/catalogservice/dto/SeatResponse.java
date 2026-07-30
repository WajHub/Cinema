package com.cinema.catalogservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SeatResponse(UUID id, String rowLabel, Integer seatNumber, BigDecimal seatPrice,
    String seatType) {
}