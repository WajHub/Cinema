package com.cinema.bookingservice.kafka;

import com.cinema.bookingservice.repository.MovieSessionRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.kafka.event.SessionChangedEvent;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageConsumer {

  private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

  private final MovieSessionRepository movieSessionRepository;
  private final SessionSeatRepository sessionSeatRepository;

  @KafkaListener(topics = "${app.kafka.topics.catalog-events}", groupId = "${spring.kafka.consumer.group-id}")
  @Transactional
  public void listen(SessionChangedEvent message) {
    movieSessionRepository.upsertSession( //
        UUID.randomUUID(), //
        UUID.fromString(message.getSessionId()), //
        message.getMovieTitle(), //
        OffsetDateTime.parse(message.getStartsAt()), //
        OffsetDateTime.parse(message.getEndsAt()));//

    if (message.getEventType()
        .name()
        .equals("CREATE")) {
      UUID catalogSessionId = UUID.fromString(message.getSessionId());
      UUID sessionId = movieSessionRepository.findByCatalogSessionId(catalogSessionId)
          .orElseThrow()
          .getId();
      message.getSeats()
          .forEach(seat -> sessionSeatRepository.upsertSeat //
          (UUID.randomUUID(), //
              sessionId, //
              UUID.fromString(seat.getSeatId()), //
              seat.getRowLabel(), //
              seat.getSeatNumber(), //
              new BigDecimal(seat.getPrice())));//
    }
    log.info("Replicated session {} with event {}", message.getSessionId(), message.getEventType());
  }
}
