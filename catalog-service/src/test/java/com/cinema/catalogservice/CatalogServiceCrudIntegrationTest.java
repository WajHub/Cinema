package com.cinema.catalogservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.repository.AuditoryRepository;
import com.cinema.catalogservice.repository.CinemaRepository;
import com.cinema.catalogservice.repository.MovieRepository;
import com.cinema.catalogservice.repository.SessionRepository;
import com.cinema.catalogservice.repository.SeatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogServiceCrudIntegrationTest {

  @Value("${local.server.port}")
  private int port;

  @Autowired
  private CinemaRepository cinemaRepository;

  @Autowired
  private AuditoryRepository auditoryRepository;

  @Autowired
  private SeatRepository seatRepository;

  @Autowired
  private MovieRepository movieRepository;

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private final HttpClient httpClient = HttpClient.newHttpClient();

  @BeforeEach
  void cleanDatabase() {
    sessionRepository.deleteAll();
    seatRepository.deleteAll();
    auditoryRepository.deleteAll();
    movieRepository.deleteAll();
    cinemaRepository.deleteAll();
  }

  @Test
  void catalogCrudHappyPath() throws Exception {
    String baseUrl = "http://localhost:" + port + "/api";

    UUID cinemaId = postId(baseUrl + "/cinemas", """
        {"name":"Central Cinema","address":"Main St 1","city":"Springfield","active":true}
        """);

    UUID auditoryOneId = postId(baseUrl + "/auditoriums", String.format("""
        {"cinemaId":"%s","name":"Auditory 1","capacity":10,"active":true,"seats":[
          {"rowLabel":"A","seatNumber":1,"seatPrice":12.50,"seatType":"STANDARD"},
          {"rowLabel":"A","seatNumber":2,"seatPrice":12.50,"seatType":"STANDARD"},
          {"rowLabel":"A","seatNumber":3,"seatPrice":12.50,"seatType":"STANDARD"},
          {"rowLabel":"A","seatNumber":4,"seatPrice":12.50,"seatType":"STANDARD"},
          {"rowLabel":"A","seatNumber":5,"seatPrice":12.50,"seatType":"STANDARD"},
          {"rowLabel":"B","seatNumber":1,"seatPrice":12.50,"seatType":"STANDARD"},
          {"rowLabel":"B","seatNumber":2,"seatPrice":12.50,"seatType":"STANDARD"},
          {"rowLabel":"B","seatNumber":3,"seatPrice":12.50,"seatType":"STANDARD"},
          {"rowLabel":"B","seatNumber":4,"seatPrice":12.50,"seatType":"STANDARD"},
          {"rowLabel":"B","seatNumber":5,"seatPrice":12.50,"seatType":"STANDARD"}
        ]}
        """, cinemaId));

    UUID auditoryTwoId = postId(baseUrl + "/auditoriums", String.format("""
        {"cinemaId":"%s","name":"Auditory 2","capacity":10,"active":true,"seats":[
          {"rowLabel":"C","seatNumber":1,"seatPrice":14.00,"seatType":"VIP"},
          {"rowLabel":"C","seatNumber":2,"seatPrice":14.00,"seatType":"VIP"},
          {"rowLabel":"C","seatNumber":3,"seatPrice":14.00,"seatType":"VIP"},
          {"rowLabel":"C","seatNumber":4,"seatPrice":14.00,"seatType":"VIP"},
          {"rowLabel":"C","seatNumber":5,"seatPrice":14.00,"seatType":"VIP"},
          {"rowLabel":"D","seatNumber":1,"seatPrice":14.00,"seatType":"VIP"},
          {"rowLabel":"D","seatNumber":2,"seatPrice":14.00,"seatType":"VIP"},
          {"rowLabel":"D","seatNumber":3,"seatPrice":14.00,"seatType":"VIP"},
          {"rowLabel":"D","seatNumber":4,"seatPrice":14.00,"seatType":"VIP"},
          {"rowLabel":"D","seatNumber":5,"seatPrice":14.00,"seatType":"VIP"}
        ]}
        """, cinemaId));

    UUID movieOneId = postId(baseUrl + "/movies", """
        {"title":"Neon City","description":"Sci-fi action in a neon future.","posterUrl":"https://example.com/neon-city.jpg","genres":["Sci-Fi","Action"],"durationMinutes":128,"language":"English","ageRating":"PG-13","releaseDate":"2026-08-01","active":true}
        """);
    UUID movieTwoId = postId(baseUrl + "/movies", """
        {"title":"Silent Harbor","description":"A tense coastal mystery.","posterUrl":"https://example.com/silent-harbor.jpg","genres":["Mystery","Drama"],"durationMinutes":104,"language":"English","ageRating":"R","releaseDate":"2026-08-08","active":true}
        """);
    UUID movieThreeId = postId(baseUrl + "/movies", """
        {"title":"Paper Planets","description":"Animated adventure across the stars.","posterUrl":"https://example.com/paper-planets.jpg","genres":["Animation","Adventure"],"durationMinutes":96,"language":"English","ageRating":"G","releaseDate":"2026-08-15","active":true}
        """);

    UUID sessionOneId = postId(baseUrl + "/sessions", String.format("""
        {"auditoryId":"%s","movieId":"%s","startsAt":"2026-08-01T18:00:00Z","endsAt":"2026-08-01T20:08:00Z","basePrice":12.50,"status":"SCHEDULED"}
      """, auditoryOneId, movieOneId));
    UUID sessionTwoId = postId(baseUrl + "/sessions", String.format("""
        {"auditoryId":"%s","movieId":"%s","startsAt":"2026-08-01T20:30:00Z","endsAt":"2026-08-01T22:14:00Z","basePrice":14.00,"status":"SCHEDULED"}
      """, auditoryTwoId, movieTwoId));
    UUID sessionThreeId = postId(baseUrl + "/sessions", String.format("""
        {"auditoryId":"%s","movieId":"%s","startsAt":"2026-08-02T16:00:00Z","endsAt":"2026-08-02T17:36:00Z","basePrice":11.00,"status":"SCHEDULED"}
      """, auditoryOneId, movieThreeId));
    UUID sessionFourId = postId(baseUrl + "/sessions", String.format("""
        {"auditoryId":"%s","movieId":"%s","startsAt":"2026-08-02T19:00:00Z","endsAt":"2026-08-02T21:08:00Z","basePrice":13.00,"status":"SCHEDULED"}
      """, auditoryTwoId, movieOneId));

    assertThat(getListSize(baseUrl + "/cinemas")).isEqualTo(1);
    assertThat(getListSize(baseUrl + "/auditoriums")).isEqualTo(2);
    assertThat(getListSize(baseUrl + "/movies")).isEqualTo(3);
    assertThat(getListSize(baseUrl + "/sessions")).isEqualTo(4);

    assertThat(List.of(cinemaId, auditoryOneId, auditoryTwoId, movieOneId, movieTwoId,
        movieThreeId, sessionOneId, sessionTwoId, sessionThreeId, sessionFourId)).doesNotContainNull();
  }

  private UUID postId(String url, String body) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(URI.create(url))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(201);
    return UUID.fromString(objectMapper.readTree(response.body()).get("id").asText());
  }

  private int getListSize(String url) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(200);
    return objectMapper.readTree(response.body()).size();
  }
}