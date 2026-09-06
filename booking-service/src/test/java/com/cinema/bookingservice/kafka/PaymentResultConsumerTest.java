package com.cinema.bookingservice.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.BookingStatus;
import com.cinema.bookingservice.entity.SeatReservationStatus;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.repository.BookingRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.kafka.event.PaymentCancelledEvent;
import com.cinema.kafka.event.PaymentCancelledEventPayload;
import com.cinema.kafka.event.PaymentCompletedEvent;
import com.cinema.kafka.event.PaymentCompletedEventPayload;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentResultConsumerTest {

  @Mock
  private BookingRepository bookingRepository;

  @Mock
  private SessionSeatRepository sessionSeatRepository;

  @InjectMocks
  private PaymentResultConsumer consumer;

  @Test
  void confirmsPendingBookingAndItsTemporarySeats() {
    UUID bookingId = UUID.randomUUID();
    BookingEntity booking = booking(bookingId, BookingStatus.PENDING);
    SessionSeatEntity seat = seat(booking, SeatReservationStatus.TEMPORARY);
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
    when(sessionSeatRepository.findByBooking_Id(bookingId)).thenReturn(List.of(seat));

    consumer.onPaymentCompleted(PaymentCompletedEvent.newBuilder()
        .setPayload(PaymentCompletedEventPayload.newBuilder()
            .setPaymentId("payment-1")
            .setBookingId(bookingId.toString())
            .setTotalPrice("20.00")
            .setCurrency("PLN")
            .setStripeCheckoutSessionId("checkout-1")
            .setCompletedAt("2026-09-03T10:00:00Z")
            .setStatus("completed")
            .build())
        .build());

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    assertThat(seat.getStatus()).isEqualTo(SeatReservationStatus.CONFIRMED);
    verify(bookingRepository).save(booking);
    verify(sessionSeatRepository).saveAll(List.of(seat));
  }

  @Test
  void cancelsPendingBookingAndReleasesItsTemporarySeats() {
    UUID bookingId = UUID.randomUUID();
    BookingEntity booking = booking(bookingId, BookingStatus.PENDING);
    SessionSeatEntity seat = seat(booking, SeatReservationStatus.TEMPORARY);
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
    when(sessionSeatRepository.findByBooking_Id(bookingId)).thenReturn(List.of(seat));

    consumer.onPaymentCancelled(PaymentCancelledEvent.newBuilder()
        .setPayload(PaymentCancelledEventPayload.newBuilder()
            .setPaymentId("payment-1")
            .setBookingId(bookingId.toString())
            .setCatalogSessionId(UUID.randomUUID().toString())
            .setCatalogSeatIds(List.of(UUID.randomUUID().toString()))
            .setTotalPrice("20.00")
            .setCurrency("PLN")
            .setStripeCheckoutSessionId("checkout-1")
            .setCancelledAt("2026-09-03T10:00:00Z")
            .setReason("expired")
            .setStatus("cancelled")
            .build())
        .build());

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    assertThat(seat.getStatus()).isEqualTo(SeatReservationStatus.AVAILABLE);
    assertThat(seat.getBooking()).isNull();
    verify(bookingRepository).save(booking);
    verify(sessionSeatRepository).saveAll(List.of(seat));
  }

  @Test
  void ignoresResultForAlreadyProcessedBooking() {
    UUID bookingId = UUID.randomUUID();
    BookingEntity booking = booking(bookingId, BookingStatus.CONFIRMED);
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

    consumer.onPaymentCompleted(PaymentCompletedEvent.newBuilder()
        .setPayload(PaymentCompletedEventPayload.newBuilder()
            .setPaymentId("payment-1")
            .setBookingId(bookingId.toString())
            .setTotalPrice("20.00")
            .setCurrency("PLN")
            .setStripeCheckoutSessionId("checkout-1")
            .setCompletedAt("2026-09-03T10:00:00Z")
            .setStatus("completed")
            .build())
        .build());

    verifyNoInteractions(sessionSeatRepository);
  }

  private BookingEntity booking(UUID id, BookingStatus status) {
    BookingEntity booking = new BookingEntity();
    booking.setId(id);
    booking.setStatus(status);
    booking.setTotalPrice(new BigDecimal("20.00"));
    return booking;
  }

  private SessionSeatEntity seat(BookingEntity booking, SeatReservationStatus status) {
    SessionSeatEntity seat = new SessionSeatEntity();
    seat.setBooking(booking);
    seat.setStatus(status);
    return seat;
  }
}
