package com.cinema.bookingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.BookingStatus;
import com.cinema.bookingservice.entity.MovieSessionEntity;
import com.cinema.bookingservice.entity.OutboxEventEntity;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.entity.UserEntity;
import com.cinema.bookingservice.repository.BookingRepository;
import com.cinema.bookingservice.repository.OutboxEventRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.bookingservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private BookingRepository bookingRepository;

  @Mock
  private SessionSeatRepository sessionSeatRepository;

  @Mock
  private OutboxEventRepository outboxEventRepository;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private BookingService bookingService;

  @Test
  void returnsAllUserBookingsWithSeats() {
    UUID userId = UUID.randomUUID();
    UUID bookingId = UUID.randomUUID();
    UserEntity user = new UserEntity();
    user.setId(userId);
    BookingEntity booking = new BookingEntity();
    booking.setId(bookingId);
    booking.setUser(user);
    booking.setTotalPrice(new BigDecimal("25.00"));
    booking.setStatus(BookingStatus.CONFIRMED);
    SessionSeatEntity seat = new SessionSeatEntity();
    seat.setCatalogSeatId(UUID.randomUUID());
    seat.setRowLabel("A");
    seat.setSeatNumber(1);
    seat.setFinalPrice(new BigDecimal("25.00"));
    seat.setStatus(com.cinema.bookingservice.entity.SeatReservationStatus.CONFIRMED);

    when(userRepository.existsById(userId)).thenReturn(true);
    when(bookingRepository.findAllByUser_IdAndStatus(userId, BookingStatus.CONFIRMED)).thenReturn(List.of(booking));
    when(sessionSeatRepository.findByBooking_Id(bookingId)).thenReturn(List.of(seat));

    var response = bookingService.findAllByUserId(userId);

    assertThat(response).hasSize(1);
    assertThat(response.get(0).bookingId()).isEqualTo(bookingId);
    assertThat(response.get(0).status()).isEqualTo(BookingStatus.CONFIRMED);
    assertThat(response.get(0).seats()).hasSize(1);
    assertThat(response.get(0).seats().get(0).rowLabel()).isEqualTo("A");
  }

  @Test
  void rejectsUnknownUser() {
    UUID userId = UUID.randomUUID();
    when(userRepository.existsById(userId)).thenReturn(false);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> bookingService.findAllByUserId(userId));

    assertThat(exception.getStatusCode().value()).isEqualTo(404);
  }

  @Test
  void cancelBooking_success_persistsRefundStartedEvent() throws Exception {
    UUID bookingId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    UserEntity user = new UserEntity();
    user.setId(userId);

    MovieSessionEntity session = new MovieSessionEntity();
    session.setStartsAt(OffsetDateTime.now().plusHours(2));

    SessionSeatEntity seat = new SessionSeatEntity();
    seat.setMovieSession(session);

    BookingEntity booking = new BookingEntity();
    booking.setId(bookingId);
    booking.setUser(user);
    booking.setStatus(BookingStatus.CONFIRMED);
    booking.setTotalPrice(new BigDecimal("30.00"));
    booking.setSessionSeatEntities(List.of(seat));

    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    bookingService.cancelBooking(bookingId);

    ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
    verify(outboxEventRepository).save(captor.capture());

    OutboxEventEntity outboxEvent = captor.getValue();
    assertThat(outboxEvent.getType()).isEqualTo("RefundStartedEvent");
    assertThat(outboxEvent.getAggregateType()).isEqualTo("booking");
    assertThat(outboxEvent.getAggregateId()).isEqualTo(bookingId);
  }

  @Test
  void cancelBooking_unconfirmedBooking_throwsBadRequest() {
    UUID bookingId = UUID.randomUUID();
    BookingEntity booking = new BookingEntity();
    booking.setId(bookingId);
    booking.setStatus(BookingStatus.PENDING);

    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> bookingService.cancelBooking(bookingId));

    assertThat(exception.getStatusCode().value()).isEqualTo(400);
    assertThat(exception.getReason()).contains("not confirmed");
  }

  @Test
  void cancelBooking_notFound_throwsNotFound() {
    UUID bookingId = UUID.randomUUID();
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> bookingService.cancelBooking(bookingId));

    assertThat(exception.getStatusCode().value()).isEqualTo(404);
  }
}
