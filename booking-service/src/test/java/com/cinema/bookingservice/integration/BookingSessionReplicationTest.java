package com.cinema.bookingservice.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.cinema.bookingservice.entity.BookingEntity;
import com.cinema.bookingservice.entity.MovieSessionEntity;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.entity.UserEntity;
import com.cinema.kafka.event.SessionChangedEvent;
import com.cinema.kafka.event.SessionChangedEventSeat;
import com.cinema.kafka.event.SessionChangedEventType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;


class BookingSessionReplicationTest extends IntegrationTestConfiguration {

  @Test
  void replicatesSessionAndSeatsOnCreateEvent() throws Exception {
    UUID sessionId = UUID.randomUUID();
    UUID seatId = UUID.randomUUID();

    kafkaTemplate.send("test.catalog-events", sessionEvent(SessionChangedEventType.CREATE, sessionId, "Inception", List.of(seatEvent(seatId, "A", 1,
        BigDecimal.valueOf(12.50)))));

    waitFor(() -> movieSessionRepository.findByCatalogSessionId(sessionId)
        .isPresent());

    MovieSessionEntity session = movieSessionRepository.findByCatalogSessionId(sessionId)
        .orElseThrow();
    assertThat(session.getMovieTitle()).isEqualTo("Inception");

    SessionSeatEntity seat = sessionSeatRepository.findBySession_IdAndSeatId(session.getId(), seatId)
        .orElseThrow();
    assertThat(seat.getRowLabel()).isEqualTo("A");
    assertThat(seat.getSeatNumber()).isEqualTo(1);
    assertThat(seat.getFinalPrice()).isEqualByComparingTo("12.50");
  }

  @Test
  void updateKeepsReservedSeatAssignments() throws Exception {
    UUID sessionId = UUID.randomUUID();
    UUID seatId = UUID.randomUUID();

    kafkaTemplate.send("test.catalog-events", sessionEvent(SessionChangedEventType.CREATE, sessionId, "Inception", List.of(seatEvent(seatId, "A", 1,
        BigDecimal.valueOf(12.50)))));

    waitFor(() -> movieSessionRepository.findByCatalogSessionId(sessionId)
        .isPresent());

    MovieSessionEntity session = movieSessionRepository.findByCatalogSessionId(sessionId)
        .orElseThrow();
    SessionSeatEntity seat = sessionSeatRepository.findBySession_IdAndSeatId(session.getId(), seatId)
        .orElseThrow();

    UserEntity user = new UserEntity();
    user.setEmail("alice@example.com");
    user.setName("Alice");
    user = userRepository.save(user);

    BookingEntity booking = new BookingEntity();
    booking.setUser(user);
    booking.setTotalPrice(BigDecimal.valueOf(12.50));
    booking.setStatus("CONFIRMED");
    booking = bookingRepository.save(booking);

    seat.setBooking(booking);
    sessionSeatRepository.save(seat);

    kafkaTemplate.send("test.catalog-events", sessionEvent(SessionChangedEventType.UPDATE, sessionId, "Inception 2", List.of(seatEvent(seatId, "B", 2,
        BigDecimal.valueOf(20.00)))));

    waitFor(() -> movieSessionRepository.findByCatalogSessionId(sessionId)
        .map(value -> "Inception 2".equals(value.getMovieTitle()))
        .orElse(false));

    MovieSessionEntity updatedSession = movieSessionRepository.findByCatalogSessionId(sessionId)
        .orElseThrow();
    SessionSeatEntity updatedSeat = sessionSeatRepository.findBySession_IdAndSeatId(updatedSession.getId(), seatId)
        .orElseThrow();

    assertThat(updatedSession.getMovieTitle()).isEqualTo("Inception 2");
    assertThat(updatedSeat.getBooking()).isNotNull();
    assertThat(updatedSeat.getBooking()
        .getId()).isEqualTo(booking.getId());
    assertThat(updatedSeat.getRowLabel()).isEqualTo("A");
    assertThat(updatedSeat.getSeatNumber()).isEqualTo(1);
  }

  private SessionChangedEvent sessionEvent(SessionChangedEventType type, UUID sessionId, String movieTitle, List<SessionChangedEventSeat> seats) {
    return SessionChangedEvent.newBuilder()
        .setEventType(type)
        .setSessionId(sessionId.toString())
        .setAuditoryId(UUID.randomUUID()
            .toString())
        .setAuditoryName("Hall 1")
        .setMovieId(UUID.randomUUID()
            .toString())
        .setMovieTitle(movieTitle)
        .setStartsAt("2026-08-12T10:00:00Z")
        .setEndsAt("2026-08-12T12:00:00Z")
        .setStatus("SCHEDULED")
        .setBasePrice("15.00")
        .setSeats(seats)
        .build();
  }

  private SessionChangedEventSeat seatEvent(UUID seatId, String rowLabel, int seatNumber, BigDecimal price) {
    return SessionChangedEventSeat.newBuilder()
        .setSeatId(seatId.toString())
        .setRowLabel(rowLabel)
        .setSeatNumber(seatNumber)
        .setPrice(price.toPlainString())
        .build();
  }

  private void waitFor(BooleanSupplier condition) throws InterruptedException {
    long deadline = System.currentTimeMillis() + 15_000;
    while (System.currentTimeMillis() < deadline) {
      if (condition.getAsBoolean()) {
        return;
      }
      Thread.sleep(200);
    }
    throw new AssertionError("Condition was not met within timeout");
  }

  @FunctionalInterface
  private interface BooleanSupplier {
    boolean getAsBoolean();
  }
}
