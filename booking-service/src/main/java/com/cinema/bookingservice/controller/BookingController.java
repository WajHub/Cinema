package com.cinema.bookingservice.controller;

import com.cinema.bookingservice.dto.BookingResponse;
import com.cinema.bookingservice.service.BookingService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class BookingController {

  private final BookingService bookingService;

  @GetMapping()
  public List<BookingResponse> getBookings(@RequestParam UUID userId) {
    return bookingService.findAllByUserId(userId);
  }
}
