package com.cinema.catalogservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.cinema.catalogservice.entity.AuditoryEntity;
import com.cinema.catalogservice.entity.CinemaEntity;
import com.cinema.catalogservice.entity.MovieEntity;
import com.cinema.catalogservice.entity.OutboxEventEntity;
import com.cinema.catalogservice.entity.SeatEntity;
import com.cinema.catalogservice.repository.AuditoryRepository;
import com.cinema.catalogservice.repository.CinemaRepository;
import com.cinema.catalogservice.repository.MovieRepository;
import com.cinema.catalogservice.repository.OutboxEventRepository;
import com.cinema.catalogservice.repository.SeatRepository;
import com.cinema.catalogservice.support.KafkaTestSupport;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class CatalogSessionReplicationTest extends KafkaTestSupport {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private CinemaRepository cinemaRepository;
  @Autowired
  private AuditoryRepository auditoryRepository;
  @Autowired
  private SeatRepository seatRepository;
  @Autowired
  private MovieRepository movieRepository;
  @Autowired
  private OutboxEventRepository outboxEventRepository;

  @BeforeEach
  void cleanDatabase() {
    outboxEventRepository.deleteAllInBatch();
    seatRepository.deleteAllInBatch();
    auditoryRepository.deleteAllInBatch();
    movieRepository.deleteAllInBatch();
    cinemaRepository.deleteAllInBatch();
  }

  @Test
  void createsOutboxEventWhenSessionIsCreated() {
    UUID cinemaId = createCinema();
    UUID movieId = createMovie();
    UUID auditoryId = createAuditory(cinemaId);
    UUID seatId = createSeat(auditoryId, "A", 1, BigDecimal.valueOf(12.50));

    var request = new CreateSessionRequest(
        auditoryId,
        movieId,
        OffsetDateTime.parse("2026-08-12T10:00:00Z"),
        OffsetDateTime.parse("2026-08-12T12:00:00Z"),
        BigDecimal.valueOf(15.00),
        "SCHEDULED");

    webTestClient.post()
        .uri("/api/v1/sessions")
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated();

    List<OutboxEventEntity> events = outboxEventRepository.findAll();
    assertThat(events).hasSize(1);
    assertThat(events.getFirst().getType()).isEqualTo("SessionChangedEvent");
    assertThat(events.getFirst().getAggregateType()).isEqualTo("session");
    assertThat(events.getFirst().getPayload()).contains(seatId.toString(), "CREATE");
  }

  private UUID createCinema() {
    CinemaEntity cinema = new CinemaEntity();
    cinema.setName("Central Cinema");
    cinema.setAddress("Main St 1");
    cinema.setCity("Springfield");
    cinema.setActive(true);
    return cinemaRepository.save(cinema).getId();
  }

  private UUID createMovie() {
    MovieEntity movie = new MovieEntity();
    movie.setTitle("Inception");
    movie.setDescription("Dream layers");
    movie.setPosterUrl("https://example.com/poster.jpg");
    movie.setGenres(new String[] {"Sci-Fi"});
    movie.setDurationMinutes(148);
    movie.setLanguage("EN");
    movie.setAgeRating("13+");
    movie.setReleaseDate(java.time.LocalDate.parse("2010-07-16"));
    movie.setActive(true);
    return movieRepository.save(movie).getId();
  }

  private UUID createAuditory(UUID cinemaId) {
    CinemaEntity cinema = cinemaRepository.findById(cinemaId).orElseThrow();
    AuditoryEntity auditory = new AuditoryEntity();
    auditory.setCinema(cinema);
    auditory.setName("Hall 1");
    auditory.setCapacity(1);
    auditory.setActive(true);
    return auditoryRepository.save(auditory).getId();
  }

  private UUID createSeat(UUID auditoryId, String rowLabel, int seatNumber, BigDecimal price) {
    AuditoryEntity auditory = auditoryRepository.findById(auditoryId).orElseThrow();
    SeatEntity seat = new SeatEntity();
    seat.setAuditory(auditory);
    seat.setRowLabel(rowLabel);
    seat.setSeatNumber(seatNumber);
    seat.setSeatPrice(price);
    seat.setSeatType("STANDARD");
    return seatRepository.save(seat).getId();
  }

  private record CreateSessionRequest(UUID auditoryId, UUID movieId, OffsetDateTime startsAt,
      OffsetDateTime endsAt, BigDecimal basePrice, String status) {
  }
}