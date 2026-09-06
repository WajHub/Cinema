package com.cinema.bookingservice.controller;

import com.cinema.bookingservice.dto.BookingReservationResponse;
import com.cinema.bookingservice.dto.SeatReservationRequest;
import com.cinema.bookingservice.exception.DoubleBokingException;
import com.cinema.bookingservice.service.SessionSeatService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/session-seats/{movieSessionId}")
public class SessionSeatController {

  private final SessionSeatService sessionSeatService;

  @PostMapping("/reserve")
  public ResponseEntity<BookingReservationResponse> reserveSeats(@PathVariable UUID movieSessionId,
      @Valid @RequestBody SeatReservationRequest request) {
    try {
      BookingReservationResponse response = sessionSeatService.reserveSeats(movieSessionId, request.seatIds(), request.userId());
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(response);
    } catch (DoubleBokingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .build();
    }
  }
}
