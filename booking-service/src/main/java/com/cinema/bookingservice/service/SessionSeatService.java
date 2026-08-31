package com.cinema.bookingservice.service;

import com.cinema.bookingservice.dto.SeatReservationResponse;
import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.BookingStatus;
import com.cinema.bookingservice.entity.MovieSessionEntity;
import com.cinema.bookingservice.entity.SeatReservationStatus;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.entity.UserEntity;
import com.cinema.bookingservice.exception.DoubleBokingException;
import com.cinema.bookingservice.repository.BookingRepository;
import com.cinema.bookingservice.repository.MovieSessionRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.bookingservice.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionSeatService {

  private static final int MINUTES_BEFORE_SESSION_END = 15;

  private final SessionSeatRepository sessionSeatRepository;
  private final BookingRepository bookingRepository;
  private final MovieSessionRepository movieSessionRepository;
  private final UserRepository userRepository;

  public SeatReservationResponse reserveSeat(UUID sessionId, UUID seatId, UUID userId) {
    MovieSessionEntity session = movieSessionRepository.findById(sessionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime cutoffTime = session.getEndsAt()
        .minusMinutes(MINUTES_BEFORE_SESSION_END);

    if (now.isAfter(cutoffTime)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reserve seats within 15 minutes of session end or after session has ended");
    }

    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    SessionSeatEntity seat = session.getSessionSeatEntities()
        .stream()
        .filter(s -> s.getId()
            .equals(seatId))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found in this session"));

    if (seat.getStatus() != SeatReservationStatus.AVAILABLE) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat is not available for reservation");
    }

    try {
      BookingEntity booking = new BookingEntity();
      booking.setUser(user);
      booking.setTotalPrice(seat.getFinalPrice());
      booking.setStatus(BookingStatus.PENDING);
      booking = bookingRepository.save(booking);

      seat.setStatus(SeatReservationStatus.TEMPORARY);
      seat.setBooking(booking);
      sessionSeatRepository.save(seat);

      return mapToResponse(seat);
    } catch (OptimisticLockingFailureException e) {
      throw new DoubleBokingException("Seat was reserved by another user. Please try again.", e);
    }
  }

  private SeatReservationResponse mapToResponse(SessionSeatEntity seat) {
    return new SeatReservationResponse(seat.getCatalogSeatId(), seat.getRowLabel(), seat.getSeatNumber(), seat.getFinalPrice(), seat.getStatus()
        .name(), seat.getBooking() != null ? seat.getBooking()
            .getId() : null);
  }
}
