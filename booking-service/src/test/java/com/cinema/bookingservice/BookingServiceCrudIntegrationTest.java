package com.cinema.bookingservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.cinema.bookingservice.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BookingServiceCrudIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void cleanDatabase() {
    userRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("Should create user successfully")
  void shouldCreateUser() {
    var request = new CreateUserRequest("alice@example.com", "Alice");

    webTestClient.post()
        .uri("/api/v1/users")
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(UserResponse.class)
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.id()).isNotNull();
          assertThat(response.email()).isEqualTo("alice@example.com");
          assertThat(response.name()).isEqualTo("Alice");
        });
  }

  @Test
  @DisplayName("Should get user by id")
  void shouldGetUserById() {
    UUID userId = createUserHelper("alice@example.com", "Alice");

    webTestClient.get()
        .uri("/api/v1/users/{id}", userId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserResponse.class)
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.id()).isEqualTo(userId);
          assertThat(response.email()).isEqualTo("alice@example.com");
          assertThat(response.name()).isEqualTo("Alice");
        });
  }

  @Test
  @DisplayName("Should update user successfully")
  void shouldUpdateUser() {
    UUID userId = createUserHelper("alice@example.com", "Alice");
    var updateRequest = new UpdateUserRequest("alice.updated@example.com", "Alice Updated");

    webTestClient.put()
        .uri("/api/v1/users/{id}", userId)
        .bodyValue(updateRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserResponse.class)
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.id()).isEqualTo(userId);
          assertThat(response.email()).isEqualTo("alice.updated@example.com");
          assertThat(response.name()).isEqualTo("Alice Updated");
        });
  }

  @Test
  @DisplayName("Should delete user successfully")
  void shouldDeleteUser() {
    UUID userId = createUserHelper("alice@example.com", "Alice");

    webTestClient.delete()
        .uri("/api/v1/users/{id}", userId)
        .exchange()
        .expectStatus()
        .isNoContent();

    assertThat(userRepository.findById(userId)).isEmpty();
  }

  private UUID createUserHelper(String email, String name) {
    var request = new CreateUserRequest(email, name);

    UserResponse response = webTestClient.post()
        .uri("/api/v1/users")
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(UserResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(response).isNotNull();
    return response.id();
  }

  private record CreateUserRequest(String email, String name) {
  }

  private record UpdateUserRequest(String email, String name) {
  }

  private record UserResponse(UUID id, String email, String name) {
  }
}
