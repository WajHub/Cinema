package com.cinema.catalogservice.service;

import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.dto.SessionRequest;
import com.cinema.catalogservice.dto.SessionResponse;
import com.cinema.catalogservice.entity.AuditoryEntity;
import com.cinema.catalogservice.entity.MovieEntity;
import com.cinema.catalogservice.entity.SessionEntity;
import com.cinema.catalogservice.repository.AuditoryRepository;
import com.cinema.catalogservice.repository.MovieRepository;
import com.cinema.catalogservice.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

  private final SessionRepository sessionRepository;
  private final AuditoryRepository auditoryRepository;
  private final MovieRepository movieRepository;

  public SessionResponse create(SessionRequest request) {
    SessionEntity session = new SessionEntity();
    apply(session, request);
    return toResponse(sessionRepository.save(session));
  }

  @Transactional(readOnly = true)
  public List<SessionResponse> findAll() {
    return sessionRepository.findAll().stream().map(this::toResponse).collect(java.util.stream.Collectors.toList());
  }

  @Transactional(readOnly = true)
  public SessionResponse findById(UUID id) {
    return toResponse(findEntity(id));
  }

  public SessionResponse update(UUID id, SessionRequest request) {
    SessionEntity session = findEntity(id);
    apply(session, request);
    return toResponse(sessionRepository.save(session));
  }

  public void delete(UUID id) {
    sessionRepository.delete(findEntity(id));
  }

  private void apply(SessionEntity session, SessionRequest request) {
    session.setAuditory(findAuditory(request.auditoryId()));
    session.setMovie(findMovie(request.movieId()));
    session.setStartsAt(request.startsAt());
    session.setEndsAt(request.endsAt());
    session.setBasePrice(request.basePrice());
    session.setStatus(request.status());
  }

  private AuditoryEntity findAuditory(UUID id) {
    return auditoryRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auditory not found"));
  }

  private MovieEntity findMovie(UUID id) {
    return movieRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
  }

  private SessionEntity findEntity(UUID id) {
    return sessionRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
  }

  private SessionResponse toResponse(SessionEntity session) {
    return new SessionResponse(session.getId(), session.getAuditory().getId(),
        session.getAuditory().getName(), session.getMovie().getId(), session.getMovie().getTitle(),
        session.getStartsAt(), session.getEndsAt(), session.getBasePrice(), session.getStatus());
  }
}