package com.cinema.bookingservice.dto;

import java.util.List;
import java.util.UUID;

public record BookingReservationResponse(
    UUID bookingId,
    List<SeatResponse> seats,
    String checkoutUrl
) {}
