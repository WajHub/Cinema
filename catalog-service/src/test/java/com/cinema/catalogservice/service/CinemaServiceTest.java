package com.cinema.catalogservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cinema.catalogservice.dto.CinemaRequest;
import com.cinema.catalogservice.entity.CinemaEntity;
import com.cinema.catalogservice.repository.CinemaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

  @Mock
  private CinemaRepository cinemaRepository;

  @InjectMocks
  private CinemaService cinemaService;

  @Test
  void createsCinema() {
    CinemaRequest request = new CinemaRequest("Central Cinema", "Main St 1", "Springfield", true);
    CinemaEntity saved = new CinemaEntity();
    saved.setId(UUID.randomUUID());
    saved.setName(request.name());
    saved.setAddress(request.address());
    saved.setCity(request.city());
    saved.setActive(request.active());
    saved.setCreatedAt(OffsetDateTime.parse("2026-07-30T12:00:00Z"));

    when(cinemaRepository.save(any(CinemaEntity.class))).thenReturn(saved);

    var response = cinemaService.create(request);

    assertThat(response.name()).isEqualTo("Central Cinema");
    assertThat(response.city()).isEqualTo("Springfield");
  }

  @Test
  void listsCinemas() {
    CinemaEntity cinema = new CinemaEntity();
    cinema.setId(UUID.randomUUID());
    cinema.setName("Central Cinema");
    cinema.setAddress("Main St 1");
    cinema.setCity("Springfield");
    cinema.setActive(true);

    when(cinemaRepository.findAll()).thenReturn(List.of(cinema));

    var responses = cinemaService.findAll();

    assertThat(responses).hasSize(1);
    assertThat(responses.getFirst()
        .name()).isEqualTo("Central Cinema");
  }
}
