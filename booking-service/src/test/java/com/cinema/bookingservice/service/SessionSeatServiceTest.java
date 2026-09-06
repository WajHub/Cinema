package com.cinema.bookingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.MovieSessionEntity;
import com.cinema.bookingservice.entity.OutboxEventEntity;
import com.cinema.bookingservice.entity.SeatReservationStatus;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.entity.UserEntity;
import com.cinema.bookingservice.repository.BookingRepository;
import com.cinema.bookingservice.repository.MovieSessionRepository;
import com.cinema.bookingservice.repository.OutboxEventRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.bookingservice.repository.UserRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionSeatServiceTest {

  @Mock
  private SessionSeatRepository sessionSeatRepository;

  @Mock
  private BookingRepository bookingRepository;

  @Mock
  private MovieSessionRepository movieSessionRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private OutboxEventRepository outboxEventRepository;

  @Spy
  private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.cinema.bookingservice.config.JacksonConfig().objectMapper();

  @InjectMocks
  private SessionSeatService service;

  @Test
  void reservesMultipleSeatsAndWritesOneOutboxEvent() {
    UUID sessionId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    SessionSeatEntity firstSeat = seat(UUID.randomUUID(), "A", 1, "10.00");
    SessionSeatEntity secondSeat = seat(UUID.randomUUID(), "A", 2, "12.50");
    MovieSessionEntity session = session(List.of(firstSeat, secondSeat), OffsetDateTime.now().plusHours(2));
    UserEntity user = new UserEntity();
    user.setId(userId);

    when(movieSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(invocation -> {
      BookingEntity booking = invocation.getArgument(0);
      booking.setId(UUID.randomUUID());
      return booking;
    });

    var response = service.reserveSeats(sessionId, List.of(firstSeat.getId(), secondSeat.getId()), userId);

    assertThat(response.seats()).hasSize(2);
    assertThat(firstSeat.getStatus()).isEqualTo(SeatReservationStatus.TEMPORARY);
    assertThat(secondSeat.getStatus()).isEqualTo(SeatReservationStatus.TEMPORARY);
    verify(bookingRepository).save(any(BookingEntity.class));
    verify(sessionSeatRepository).saveAll(List.of(firstSeat, secondSeat));
    verify(outboxEventRepository).save(any(OutboxEventEntity.class));
  }

  private SessionSeatEntity seat(UUID id, String row, int number, String price) {
    SessionSeatEntity seat = new SessionSeatEntity();
    seat.setId(id);
    seat.setCatalogSeatId(UUID.randomUUID());
    seat.setRowLabel(row);
    seat.setSeatNumber(number);
    seat.setFinalPrice(new BigDecimal(price));
    seat.setStatus(SeatReservationStatus.AVAILABLE);
    return seat;
  }

  private MovieSessionEntity session(List<SessionSeatEntity> seats, OffsetDateTime endsAt) {
    MovieSessionEntity session = new MovieSessionEntity();
    session.setCatalogSessionId(UUID.randomUUID());
    session.setEndsAt(endsAt);
    session.setSessionSeatEntities(seats);
    return session;
  }
}
