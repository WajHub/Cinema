package com.cinema.catalogservice.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CatalogServiceCrudIntegrationTest extends IntegrationTestConfiguration {


  @Test
  @DisplayName("Should create cinema successfully")
  void shouldCreateCinema() {
    // given
    var request = new CreateCinemaRequest("Central Cinema", "Main St 1", "Springfield", true);

    // when & then
    webTestClient.post()
        .uri("/api/v1/cinemas")
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(IdResponse.class)
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.id()).isNotNull();
        });
  }

  @Test
  @DisplayName("Should create auditorium assigned to cinema")
  void shouldCreateAuditorium() {
    // given
    UUID cinemaId = createCinemaHelper("Central Cinema");
    var seat = new SeatDto("A", 1, 12.50, "STANDARD");
    var request = new CreateAuditoryRequest(cinemaId, "Auditory 1", 10, true, List.of(seat));

    // when & then
    webTestClient.post()
        .uri("/api/v1/auditoriums")
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(IdResponse.class)
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.id()).isNotNull();
        });
  }

  private UUID createCinemaHelper(String name) {
    var request = new CreateCinemaRequest(name, "Main St 1", "Springfield", true);

    IdResponse response = webTestClient.post()
        .uri("/api/v1/cinemas")
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(IdResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(response).isNotNull();
    return response.id();
  }

  private record IdResponse(UUID id) {
  }
  private record CreateCinemaRequest(String name, String address, String city, boolean active) {
  }
  private record CreateAuditoryRequest(UUID cinemaId, String name, int capacity, boolean active, List<SeatDto> seats) {
  }
  private record SeatDto(String rowLabel, int seatNumber, double seatPrice, String seatType) {
  }
}
