package com.cinema.bookingservice.service;

import com.cinema.bookingservice.dto.BookingReservationResponse;
import com.cinema.bookingservice.dto.SeatResponse;
import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.BookingStatus;
import com.cinema.bookingservice.entity.MovieSessionEntity;
import com.cinema.bookingservice.entity.OutboxEventEntity;
import com.cinema.bookingservice.entity.SeatReservationStatus;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.entity.UserEntity;
import com.cinema.bookingservice.exception.DoubleBokingException;
import com.cinema.bookingservice.kafka.event.PaymentStartedEventPayload;
import com.cinema.bookingservice.repository.BookingRepository;
import com.cinema.bookingservice.repository.MovieSessionRepository;
import com.cinema.bookingservice.repository.OutboxEventRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.bookingservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  public BookingReservationResponse reserveSeats(UUID movieSessionId, List<UUID> sessionSeatIds, UUID userId) {
    MovieSessionEntity session = movieSessionRepository.findById(movieSessionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime cutoffTime = session.getEndsAt()
        .minusMinutes(MINUTES_BEFORE_SESSION_END);

    if (now.isAfter(cutoffTime) && false) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reserve seats within 15 minutes of session end or after session has ended");
    }

    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    List<SessionSeatEntity> seats = session.getSessionSeatEntities()
        .stream()
        .filter(sessionSeatEntity -> sessionSeatIds.contains(sessionSeatEntity.getId()))
        .toList();
    seats.forEach(seat -> {
      if (seat.getStatus() != SeatReservationStatus.AVAILABLE) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat " + seat.getCatalogSeatId() + " is not available for reservation");
      }
    });

    try {
      BigDecimal totalPrice = seats.stream()
          .map(SessionSeatEntity::getFinalPrice)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BookingEntity booking = new BookingEntity();
      booking.setUser(user);
      booking.setTotalPrice(totalPrice);
      booking.setStatus(BookingStatus.PENDING);
      BookingEntity savedBookingEntity = bookingRepository.save(booking);

      seats.forEach(seat -> {
        seat.setStatus(SeatReservationStatus.TEMPORARY);
        seat.setBooking(savedBookingEntity);
      });
      // Persist outbox event for payment processing
      persistPaymentStartedEvent(savedBookingEntity, session, seats);

      sessionSeatRepository.saveAll(seats);

      return new BookingReservationResponse(booking.getId(), mapToSeatResponses(seats));
    } catch (OptimisticLockingFailureException e) {
      throw new DoubleBokingException("One or more seats were reserved by another user. Please try again.", e);
    }
  }

  private List<SeatResponse> mapToSeatResponses(List<SessionSeatEntity> seats) {
    return seats.stream()
        .map(seat -> new SeatResponse(seat.getCatalogSeatId(), seat.getRowLabel(), seat.getSeatNumber(), seat.getFinalPrice(), seat.getStatus()
            .name()))
        .toList();
  }

  private void persistPaymentStartedEvent(BookingEntity booking, MovieSessionEntity session, List<SessionSeatEntity> seats) {
    try {
      List<String> catalogSeatIds = seats.stream()
          .map(SessionSeatEntity::getCatalogSeatId)
          .map(UUID::toString)
          .collect(Collectors.toList());

      PaymentStartedEventPayload payload = new PaymentStartedEventPayload(booking.getId()
          .toString(), booking.getUser()
              .getId()
              .toString(), session.getCatalogSessionId()
                  .toString(), catalogSeatIds, booking.getTotalPrice()
                      .toPlainString(), booking.getTotalPrice()
                          .toPlainString(), OffsetDateTime.now()
                              .toString());

      OutboxEventEntity outboxEvent = new OutboxEventEntity();
      outboxEvent.setAggregateType("booking");
      outboxEvent.setAggregateId(booking.getId());
      outboxEvent.setType("PaymentStartedEvent");
      outboxEvent.setPayload(objectMapper.writeValueAsString(payload));

      outboxEventRepository.save(outboxEvent);
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to persist PaymentStartedEvent to outbox", exception);
    }
  }
}
