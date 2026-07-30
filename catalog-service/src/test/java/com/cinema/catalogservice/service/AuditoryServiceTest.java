package com.cinema.catalogservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.cinema.catalogservice.dto.AuditoryRequest;
import com.cinema.catalogservice.dto.SeatRequest;
import com.cinema.catalogservice.entity.AuditoryEntity;
import com.cinema.catalogservice.entity.CinemaEntity;
import com.cinema.catalogservice.entity.SeatEntity;
import com.cinema.catalogservice.repository.AuditoryRepository;
import com.cinema.catalogservice.repository.CinemaRepository;
import com.cinema.catalogservice.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditoryServiceTest {

  @Mock
  private AuditoryRepository auditoryRepository;

  @Mock
  private CinemaRepository cinemaRepository;

  @Mock
  private SeatRepository seatRepository;

  @InjectMocks
  private AuditoryService auditoryService;

  @Test
  void createsAuditoryWithSeats() {
    UUID cinemaId = UUID.randomUUID();
    CinemaEntity cinema = new CinemaEntity();
    cinema.setId(cinemaId);
    cinema.setName("Central Cinema");

    UUID auditoryId = UUID.randomUUID();
    AuditoryEntity savedAuditory = new AuditoryEntity();
    savedAuditory.setId(auditoryId);
    savedAuditory.setCinema(cinema);
    savedAuditory.setName("Auditory 1");
    savedAuditory.setCapacity(2);
    savedAuditory.setActive(true);

    SeatEntity seat = new SeatEntity();
    seat.setId(UUID.randomUUID());
    seat.setAuditory(savedAuditory);
    seat.setRowLabel("A");
    seat.setSeatNumber(1);
    seat.setSeatPrice(BigDecimal.valueOf(12.50));
    seat.setSeatType("STANDARD");

    when(cinemaRepository.findById(cinemaId)).thenReturn(Optional.of(cinema));
    when(auditoryRepository.save(any(AuditoryEntity.class))).thenReturn(savedAuditory);
    when(seatRepository.findAllByAuditory_Id(auditoryId)).thenReturn(List.of(seat));

    var response = auditoryService.create(new AuditoryRequest(
        cinemaId,
        "Auditory 1",
        2,
        true,
        List.of(new SeatRequest("A", 1, BigDecimal.valueOf(12.50), "STANDARD"))));

    assertThat(response.cinemaId()).isEqualTo(cinemaId);
    assertThat(response.seats()).hasSize(1);
    verify(seatRepository).saveAll(any());
  }
}