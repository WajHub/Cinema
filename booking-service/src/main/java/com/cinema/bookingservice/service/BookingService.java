package com.cinema.bookingservice.service;

import com.cinema.bookingservice.dto.BookingResponse;
import com.cinema.bookingservice.dto.SeatResponse;
import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.BookingStatus;
import com.cinema.bookingservice.entity.OutboxEventEntity;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.kafka.event.RefundStartedEventPayload;
import com.cinema.bookingservice.repository.BookingRepository;
import com.cinema.bookingservice.repository.OutboxEventRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.bookingservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

  private final UserRepository userRepository;
  private final BookingRepository bookingRepository;
  private final SessionSeatRepository sessionSeatRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  @Value("${app.booking.min-minutes-before-session-for-refund:15}")
  private long minMinutesBeforeSessionForRefund;

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
    return SeatResponse.builder()
        .sessionSeatId(seat.getId())
        .catalogSeatId(seat.getCatalogSeatId())
        .rowLabel(seat.getRowLabel())
        .seatNumber(seat.getSeatNumber())
        .finalPrice(seat.getFinalPrice())
        .status(seat.getStatus()
            .name())
        .build();
  }

  public void cancelBooking(UUID bookingId) {
    BookingEntity booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    if (booking.getStatus() != BookingStatus.CONFIRMED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is not confirmed!");
    }
    var movieSession = booking.getSessionSeatEntities()
        .stream()
        .findAny()
        .map(SessionSeatEntity::getMovieSession)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
    var movieStartTime = movieSession.getStartsAt();
    var validTimeForRefund = OffsetDateTime.now()
        .plusMinutes(minMinutesBeforeSessionForRefund);
    if (movieStartTime.isBefore(validTimeForRefund) && false) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot refund booking before 15 minutes of session start");
    }
    persistRefundStartedEvent(booking);
  }

  private void persistRefundStartedEvent(BookingEntity booking) {
    try {
      RefundStartedEventPayload payload = RefundStartedEventPayload.newBuilder()
          .setBookingId(booking.getId()
              .toString())
          .setUserId(booking.getUser()
              .getId()
              .toString())
          .setTotalPrice(booking.getTotalPrice()
              .toPlainString())
          .setCurrency("PLN")
          .setCreatedAt(OffsetDateTime.now()
              .toString())
          .build();

      OutboxEventEntity outboxEvent = new OutboxEventEntity();
      outboxEvent.setAggregateType("booking");
      outboxEvent.setAggregateId(booking.getId());
      outboxEvent.setType("RefundStartedEvent");
      outboxEvent.setPayload(objectMapper.writeValueAsString(payload));

      outboxEventRepository.save(outboxEvent);
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to persist RefundStartedEvent to outbox", exception);
    }
  }
}
