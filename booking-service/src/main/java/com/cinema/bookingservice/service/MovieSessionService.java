package com.cinema.bookingservice.service;

import com.cinema.bookingservice.dto.MovieSessionResponse;
import com.cinema.bookingservice.dto.SessionSeatResponse;
import com.cinema.bookingservice.entity.MovieSessionEntity;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import com.cinema.bookingservice.repository.MovieSessionRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieSessionService {

  private final MovieSessionRepository movieSessionRepository;

  @Transactional(readOnly = true)
  public List<MovieSessionResponse> findAll() {
    return movieSessionRepository.findAllBy()
        .stream()
        .map(this::toResponse)
        .toList();
  }

  private MovieSessionResponse toResponse(MovieSessionEntity session) {
    List<SessionSeatResponse> seats = session.getSessionSeatEntities()
        .stream()
        .sorted(Comparator.comparing(SessionSeatEntity::getRowLabel)
            .thenComparing(SessionSeatEntity::getSeatNumber))
        .map(this::toSeatResponse)
        .toList();

    return MovieSessionResponse.builder()
        .id(session.getId())
        .catalogSessionId(session.getCatalogSessionId())
        .movieTitle(session.getMovieTitle())
        .startsAt(session.getStartsAt())
        .endsAt(session.getEndsAt())
        .seats(seats)
        .build();
  }

  private SessionSeatResponse toSeatResponse(SessionSeatEntity seat) {
    return SessionSeatResponse.builder()
        .id(seat.getId())
        .rowLabel(seat.getRowLabel())
        .seatNumber(seat.getSeatNumber())
        .seatPrice(seat.getFinalPrice())
        .status(seat.getStatus()
            .name())
        .build();
  }
}
