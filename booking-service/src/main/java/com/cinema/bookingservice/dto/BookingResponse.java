package com.cinema.bookingservice.dto;

import com.cinema.bookingservice.entity.BookingStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
    UUID bookingId,
    UUID userId,
    BigDecimal totalPrice,
    BookingStatus status,
    List<SeatResponse> seats) {}
