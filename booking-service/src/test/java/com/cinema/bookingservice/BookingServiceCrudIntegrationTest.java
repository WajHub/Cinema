package com.cinema.bookingservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import com.cinema.bookingservice.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingServiceCrudIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private final HttpClient httpClient = HttpClient.newHttpClient();

  @BeforeEach
  void cleanDatabase() {
    userRepository.deleteAll();
  }

  @Test
  void userCrudHappyPath() throws Exception {
    String baseUrl = "http://localhost:" + port + "/api/users";

    HttpRequest createRequest = HttpRequest.newBuilder(URI.create(baseUrl))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .POST(HttpRequest.BodyPublishers.ofString("""
        {"email":"alice@example.com","name":"Alice"}
        """))
      .build();
    HttpResponse<String> createdResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
    assertThat(createdResponse.statusCode()).isEqualTo(201);

    JsonNode createdNode = objectMapper.readTree(createdResponse.body());
    UUID id = UUID.fromString(createdNode.get("id").asText());

    HttpRequest getRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/" + id)).GET().build();
    HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
    assertThat(getResponse.statusCode()).isEqualTo(200);

    HttpRequest updateRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/" + id))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .method(HttpMethod.PUT.name(), HttpRequest.BodyPublishers.ofString("""
        {"email":"alice.updated@example.com","name":"Alice Updated"}
        """))
      .build();
    HttpResponse<String> updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());
    assertThat(updateResponse.statusCode()).isEqualTo(200);

    HttpRequest deleteRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/" + id)).DELETE().build();
    HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
    assertThat(deleteResponse.statusCode()).isEqualTo(204);

    assertThat(userRepository.findById(id)).isEmpty();
  }
}