package com.cinema.catalogservice.service;

import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.dto.CinemaRequest;
import com.cinema.catalogservice.dto.CinemaResponse;
import com.cinema.catalogservice.entity.CinemaEntity;
import com.cinema.catalogservice.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class CinemaService {

  private final CinemaRepository cinemaRepository;

  public CinemaResponse create(CinemaRequest request) {
    CinemaEntity cinema = new CinemaEntity();
    cinema.setName(request.name());
    cinema.setAddress(request.address());
    cinema.setCity(request.city());
    cinema.setActive(request.active());
    return toResponse(cinemaRepository.save(cinema));
  }

  @Transactional(readOnly = true)
  public List<CinemaResponse> findAll() {
    return cinemaRepository.findAll().stream().map(this::toResponse).collect(java.util.stream.Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CinemaResponse findById(UUID id) {
    return toResponse(findEntity(id));
  }

  public CinemaResponse update(UUID id, CinemaRequest request) {
    CinemaEntity cinema = findEntity(id);
    cinema.setName(request.name());
    cinema.setAddress(request.address());
    cinema.setCity(request.city());
    cinema.setActive(request.active());
    return toResponse(cinemaRepository.save(cinema));
  }

  public void delete(UUID id) {
    cinemaRepository.delete(findEntity(id));
  }

  private CinemaEntity findEntity(UUID id) {
    return cinemaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cinema not found"));
  }

  private CinemaResponse toResponse(CinemaEntity cinema) {
    return new CinemaResponse(cinema.getId(), cinema.getName(), cinema.getAddress(),
        cinema.getCity(), cinema.isActive(), cinema.getCreatedAt());
  }
}