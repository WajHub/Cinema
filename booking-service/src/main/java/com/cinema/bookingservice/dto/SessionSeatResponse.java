package com.cinema.bookingservice.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SessionSeatResponse( //
    UUID id, //
    String rowLabel, //
    Integer seatNumber, //
    BigDecimal seatPrice, //
    String status //
) {
}
