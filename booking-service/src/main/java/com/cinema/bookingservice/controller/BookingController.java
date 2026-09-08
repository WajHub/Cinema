package com.cinema.bookingservice.controller;

import com.cinema.bookingservice.dto.BookingReservationResponse;
import com.cinema.bookingservice.dto.BookingResponse;
import com.cinema.bookingservice.dto.SeatReservationRequest;
import com.cinema.bookingservice.exception.DoubleBokingException;
import com.cinema.bookingservice.service.BookingService;
import com.cinema.bookingservice.service.SessionSeatService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class BookingController {

  private final BookingService bookingService;
  private final SessionSeatService sessionSeatService;

  @PostMapping
  public ResponseEntity<?> reserveSeats(
      @Valid @RequestBody SeatReservationRequest request) {
    try {
      BookingReservationResponse response = sessionSeatService.reserveSeats(
          request.movieSessionId(), request.seatIds(), request.userId());
      return ResponseEntity.ok(response);
    } catch (DoubleBokingException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ReservationConflictResponse(
              "One or more seats were already reserved by another user. Please try again.",
              request.movieSessionId(),
              request.seatIds()
          ));
    }
  }

  public record ReservationConflictResponse(
      String message,
      UUID movieSessionId,
      List<UUID> seatIds
  ) {}

  @GetMapping()
  public List<BookingResponse> getBookings(@RequestParam UUID userId) {
    return bookingService.findAllByUserId(userId);
  }

  @PostMapping("{bookingId}/cancel")
  public ResponseEntity<Void> cancelBooking(@PathVariable UUID bookingId) {
    bookingService.cancelBooking(bookingId);
    return ResponseEntity.accepted()
        .build();
  }

}
