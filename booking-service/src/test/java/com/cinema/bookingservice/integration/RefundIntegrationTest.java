package com.cinema.bookingservice.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.BookingStatus;
import com.cinema.bookingservice.entity.MovieSessionEntity;
import com.cinema.bookingservice.entity.OutboxEventEntity;
import com.cinema.bookingservice.entity.SeatReservationStatus;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.entity.UserEntity;
import com.cinema.bookingservice.kafka.RefundResultConsumer;
import com.cinema.kafka.event.RefundCompletedEvent;
import com.cinema.kafka.event.RefundCompletedEventPayload;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RefundIntegrationTest extends IntegrationTestConfiguration {

  @Autowired
  private RefundResultConsumer refundResultConsumer;

  @Test
  @DisplayName("Should cancel confirmed booking via endpoint and persist RefundStartedEvent to outbox")
  void shouldCancelBookingAndPersistRefundStartedOutboxEvent() {
    BookingEntity booking = createConfirmedBookingWithSeat();

    webTestClient.post()
        .uri("/api/v1/bookings/{bookingId}/cancel", booking.getId())
        .exchange()
        .expectStatus()
        .isAccepted();

    List<OutboxEventEntity> outboxEvents = outboxEventRepository.findAll();
    assertThat(outboxEvents).hasSize(1);
    OutboxEventEntity outboxEvent = outboxEvents.get(0);
    assertThat(outboxEvent.getType()).isEqualTo("RefundStartedEvent");
    assertThat(outboxEvent.getAggregateType()).isEqualTo("booking");
    assertThat(outboxEvent.getAggregateId()).isEqualTo(booking.getId());
    assertThat(outboxEvent.getPayload()).contains(booking.getId().toString());
  }

  @Test
  @DisplayName("Should process RefundCompletedEvent, cancel booking and set seats to AVAILABLE")
  void shouldProcessRefundCompletedEventAndReleaseSeats() {
    BookingEntity booking = createConfirmedBookingWithSeat();

    refundResultConsumer.onRefundCompleted(RefundCompletedEvent.newBuilder()
        .setPayload(RefundCompletedEventPayload.newBuilder()
            .setBookingId(booking.getId().toString())
            .setTotalPrice("30.00")
            .setCreatedAt(OffsetDateTime.now().toString())
            .build())
        .build());

    BookingEntity updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
    assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);

    List<SessionSeatEntity> seats = sessionSeatRepository.findByBooking_Id(booking.getId());
    assertThat(seats).isNotEmpty();
    assertThat(seats).allMatch(seat -> seat.getStatus() == SeatReservationStatus.AVAILABLE);
  }

  private BookingEntity createConfirmedBookingWithSeat() {
    UserEntity user = new UserEntity();
    user.setEmail("user-" + UUID.randomUUID() + "@example.com");
    user.setName("John Doe");
    user = userRepository.save(user);

    MovieSessionEntity session = new MovieSessionEntity();
    session.setCatalogSessionId(UUID.randomUUID());
    session.setMovieTitle("Interstellar");
    session.setStartsAt(OffsetDateTime.now().plusHours(3));
    session.setEndsAt(OffsetDateTime.now().plusHours(5));
    session = movieSessionRepository.save(session);

    BookingEntity booking = new BookingEntity();
    booking.setUser(user);
    booking.setTotalPrice(new BigDecimal("30.00"));
    booking.setStatus(BookingStatus.CONFIRMED);
    booking = bookingRepository.save(booking);

    SessionSeatEntity seat = new SessionSeatEntity();
    seat.setMovieSession(session);
    seat.setBooking(booking);
    seat.setCatalogSeatId(UUID.randomUUID());
    seat.setRowLabel("B");
    seat.setSeatNumber(5);
    seat.setFinalPrice(new BigDecimal("30.00"));
    seat.setStatus(SeatReservationStatus.CONFIRMED);
    sessionSeatRepository.save(seat);

    return booking;
  }
}
