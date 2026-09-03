package com.cinema.bookingservice.kafka;

import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.BookingStatus;
import com.cinema.bookingservice.entity.SeatReservationStatus;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.repository.BookingRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.kafka.event.PaymentCancelledEvent;
import com.cinema.kafka.event.PaymentCompletedEvent;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

  private final BookingRepository bookingRepository;
  private final SessionSeatRepository sessionSeatRepository;

  @KafkaListener(topics = "${app.kafka.topics.payment-completed}", groupId = "${spring.kafka.consumer.group-id}")
  @Transactional
  public void onPaymentCompleted(PaymentCompletedEvent event) {
    BookingEntity booking = findBooking(event.getBookingId().toString());
    if (booking == null || booking.getStatus() != BookingStatus.PENDING) {
      return;
    }

    booking.setStatus(BookingStatus.CONFIRMED);
    bookingRepository.save(booking);

    List<SessionSeatEntity> seats = sessionSeatRepository.findByBooking_Id(booking.getId());
    seats.stream()
        .filter(seat -> seat.getStatus() == SeatReservationStatus.TEMPORARY)
        .forEach(seat -> seat.setStatus(SeatReservationStatus.CONFIRMED));
    sessionSeatRepository.saveAll(seats);
  }

  @KafkaListener(topics = "${app.kafka.topics.payment-cancelled}", groupId = "${spring.kafka.consumer.group-id}")
  @Transactional
  public void onPaymentCancelled(PaymentCancelledEvent event) {
    BookingEntity booking = findBooking(event.getBookingId().toString());
    if (booking == null || booking.getStatus() != BookingStatus.PENDING) {
      return;
    }

    booking.setStatus(BookingStatus.CANCELLED);
    bookingRepository.save(booking);

    List<SessionSeatEntity> seats = sessionSeatRepository.findByBooking_Id(booking.getId());
    seats.stream()
        .filter(seat -> seat.getStatus() == SeatReservationStatus.TEMPORARY)
        .forEach(seat -> {
          seat.setStatus(SeatReservationStatus.AVAILABLE);
          seat.setBooking(null);
        });
    sessionSeatRepository.saveAll(seats);
  }

  private BookingEntity findBooking(String bookingId) {
    try {
      return bookingRepository.findById(UUID.fromString(bookingId)).orElse(null);
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }
}
