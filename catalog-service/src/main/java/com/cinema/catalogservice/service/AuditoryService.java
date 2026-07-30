package com.cinema.catalogservice.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.dto.AuditoryRequest;
import com.cinema.catalogservice.dto.AuditoryResponse;
import com.cinema.catalogservice.dto.SeatRequest;
import com.cinema.catalogservice.dto.SeatResponse;
import com.cinema.catalogservice.entity.AuditoryEntity;
import com.cinema.catalogservice.entity.CinemaEntity;
import com.cinema.catalogservice.entity.SeatEntity;
import com.cinema.catalogservice.repository.AuditoryRepository;
import com.cinema.catalogservice.repository.CinemaRepository;
import com.cinema.catalogservice.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditoryService {

  private final AuditoryRepository auditoryRepository;
  private final CinemaRepository cinemaRepository;
  private final SeatRepository seatRepository;

  public AuditoryResponse create(AuditoryRequest request) {
    CinemaEntity cinema = findCinema(request.cinemaId());
    AuditoryEntity auditory = new AuditoryEntity();
    auditory.setCinema(cinema);
    auditory.setName(request.name());
    auditory.setCapacity(request.capacity());
    auditory.setActive(request.active());
    AuditoryEntity saved = auditoryRepository.save(auditory);
    saveSeats(saved, request.seats());
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<AuditoryResponse> findAll() {
    return auditoryRepository.findAll().stream().map(this::toResponse).collect(java.util.stream.Collectors.toList());
  }

  @Transactional(readOnly = true)
  public AuditoryResponse findById(UUID id) {
    return toResponse(findEntity(id));
  }

  public AuditoryResponse update(UUID id, AuditoryRequest request) {
    AuditoryEntity auditory = findEntity(id);
    auditory.setCinema(findCinema(request.cinemaId()));
    auditory.setName(request.name());
    auditory.setCapacity(request.capacity());
    auditory.setActive(request.active());
    AuditoryEntity saved = auditoryRepository.save(auditory);
    replaceSeats(saved, request.seats());
    return toResponse(saved);
  }

  public void delete(UUID id) {
    replaceSeats(findEntity(id), List.of());
    auditoryRepository.delete(findEntity(id));
  }

  private void replaceSeats(AuditoryEntity auditory, List<SeatRequest> seatRequests) {
    List<SeatEntity> existingSeats = seatRepository.findAllByAuditory_Id(auditory.getId());
    if (!existingSeats.isEmpty()) {
      seatRepository.deleteAll(existingSeats);
    }
    saveSeats(auditory, seatRequests);
  }

  private void saveSeats(AuditoryEntity auditory, List<SeatRequest> seatRequests) {
    List<SeatEntity> seats = seatRequests.stream().map(request -> {
      SeatEntity seat = new SeatEntity();
      seat.setAuditory(auditory);
      seat.setRowLabel(request.rowLabel());
      seat.setSeatNumber(request.seatNumber());
      seat.setSeatPrice(request.seatPrice());
      seat.setSeatType(request.seatType());
      return seat;
    }).collect(java.util.stream.Collectors.toList());
    seatRepository.saveAll(seats);
  }

  private AuditoryEntity findEntity(UUID id) {
    return auditoryRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auditory not found"));
  }

  private CinemaEntity findCinema(UUID id) {
    return cinemaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cinema not found"));
  }

  private AuditoryResponse toResponse(AuditoryEntity auditory) {
    List<SeatResponse> seats = seatRepository.findAllByAuditory_Id(auditory.getId()).stream()
        .sorted(Comparator.comparing(SeatEntity::getRowLabel).thenComparing(SeatEntity::getSeatNumber))
        .map(this::toSeatResponse)
        .collect(java.util.stream.Collectors.toList());
    return new AuditoryResponse(auditory.getId(), auditory.getCinema().getId(),
        auditory.getCinema().getName(), auditory.getName(), auditory.getCapacity(),
        auditory.isActive(), seats);
  }

  private SeatResponse toSeatResponse(SeatEntity seat) {
    return new SeatResponse(seat.getId(), seat.getRowLabel(), seat.getSeatNumber(),
        seat.getSeatPrice(), seat.getSeatType());
  }
}