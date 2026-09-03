package com.cinema.bookingservice.service;

import com.cinema.bookingservice.dto.BookingResponse;
import com.cinema.bookingservice.dto.SeatResponse;
import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.BookingStatus;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.repository.BookingRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.bookingservice.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

  private final UserRepository userRepository;
  private final BookingRepository bookingRepository;
  private final SessionSeatRepository sessionSeatRepository;

  public List<BookingResponse> findAllByUserId(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    return bookingRepository.findAllByUser_IdAndStatus(userId, BookingStatus.CONFIRMED)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  private BookingResponse toResponse(BookingEntity booking) {
    List<SeatResponse> seats = sessionSeatRepository.findByBooking_Id(booking.getId())
        .stream()
        .map(this::toSeatResponse)
        .toList();
    return new BookingResponse(booking.getId(), booking.getUser()
        .getId(), booking.getTotalPrice(), booking.getStatus(), seats);
  }

  private SeatResponse toSeatResponse(SessionSeatEntity seat) {
    return new SeatResponse(seat.getCatalogSeatId(), seat.getRowLabel(), seat.getSeatNumber(), seat.getFinalPrice(), seat.getStatus()
        .name());
  }
}
