package com.cinema.bookingservice.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.BookingStatus;
import com.cinema.bookingservice.entity.MovieSessionEntity;
import com.cinema.bookingservice.entity.SeatReservationStatus;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.entity.UserEntity;
import com.cinema.bookingservice.kafka.PaymentResultConsumer;
import com.cinema.kafka.event.PaymentCancelledEvent;
import com.cinema.kafka.event.PaymentCancelledEventPayload;
import com.cinema.kafka.event.PaymentCompletedEvent;
import com.cinema.kafka.event.PaymentCompletedEventPayload;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PaymentResultConsumerIntegrationTest extends IntegrationTestConfiguration {

  @Autowired
  private PaymentResultConsumer paymentResultConsumer;

  @Test
  void persistsPaymentCompletionAndSeatConfirmation() {
    BookingEntity booking = createBooking(BookingStatus.PENDING);
    SessionSeatEntity seat = createSeat(booking, SeatReservationStatus.TEMPORARY);

    paymentResultConsumer.onPaymentCompleted(PaymentCompletedEvent.newBuilder()
        .setPayload(PaymentCompletedEventPayload.newBuilder()
            .setPaymentId("payment-1")
            .setBookingId(booking.getId().toString())
            .setTotalPrice("20.00")
            .setCurrency("PLN")
            .setStripeCheckoutSessionId("checkout-1")
            .setCompletedAt(OffsetDateTime.now().toString())
            .setStatus("completed")
            .build())
        .build());

    BookingEntity savedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
    SessionSeatEntity savedSeat = sessionSeatRepository.findById(seat.getId()).orElseThrow();
    assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    assertThat(savedSeat.getStatus()).isEqualTo(SeatReservationStatus.CONFIRMED);
  }

  @Test
  void persistsCancellationAndReleasesSeat() {
    BookingEntity booking = createBooking(BookingStatus.PENDING);
    SessionSeatEntity seat = createSeat(booking, SeatReservationStatus.TEMPORARY);

    paymentResultConsumer.onPaymentCancelled(PaymentCancelledEvent.newBuilder()
        .setPayload(PaymentCancelledEventPayload.newBuilder()
            .setPaymentId("payment-1")
            .setBookingId(booking.getId().toString())
            .setCatalogSessionId(UUID.randomUUID().toString())
            .setCatalogSeatIds(List.of(UUID.randomUUID().toString()))
            .setTotalPrice("20.00")
            .setCurrency("PLN")
            .setStripeCheckoutSessionId("checkout-1")
            .setCancelledAt(OffsetDateTime.now().toString())
            .setReason("expired")
            .setStatus("cancelled")
            .build())
        .build());

    BookingEntity savedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
    SessionSeatEntity savedSeat = sessionSeatRepository.findById(seat.getId()).orElseThrow();
    assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    assertThat(savedSeat.getStatus()).isEqualTo(SeatReservationStatus.AVAILABLE);
    assertThat(savedSeat.getBooking()).isNull();
  }

  private BookingEntity createBooking(BookingStatus status) {
    UserEntity user = new UserEntity();
    user.setEmail(UUID.randomUUID() + "@example.com");
    user.setName("Alice");
    user = userRepository.save(user);

    BookingEntity booking = new BookingEntity();
    booking.setUser(user);
    booking.setTotalPrice(new BigDecimal("20.00"));
    booking.setStatus(status);
    return bookingRepository.save(booking);
  }

  private SessionSeatEntity createSeat(BookingEntity booking, SeatReservationStatus status) {
    MovieSessionEntity session = new MovieSessionEntity();
    session.setCatalogSessionId(UUID.randomUUID());
    session.setMovieTitle("Inception");
    session.setStartsAt(OffsetDateTime.now().plusHours(1));
    session.setEndsAt(OffsetDateTime.now().plusHours(3));
    session = movieSessionRepository.save(session);

    SessionSeatEntity seat = new SessionSeatEntity();
    seat.setMovieSession(session);
    seat.setBooking(booking);
    seat.setCatalogSeatId(UUID.randomUUID());
    seat.setRowLabel("A");
    seat.setSeatNumber(1);
    seat.setFinalPrice(new BigDecimal("20.00"));
    seat.setStatus(status);
    return sessionSeatRepository.save(seat);
  }
}
