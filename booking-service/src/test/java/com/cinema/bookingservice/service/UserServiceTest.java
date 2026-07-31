package com.cinema.bookingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.cinema.bookingservice.dto.UserRequest;
import com.cinema.bookingservice.entity.UserEntity;
import com.cinema.bookingservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Test
  void createsUser() {
    UserRequest request = new UserRequest("alice@example.com", "Alice");
    UserEntity saved = new UserEntity();
    saved.setId(UUID.randomUUID());
    saved.setEmail(request.email());
    saved.setName(request.name());

    when(userRepository.save(any(UserEntity.class))).thenReturn(saved);

    var response = userService.create(request);

    assertThat(response.email()).isEqualTo("alice@example.com");
    assertThat(response.name()).isEqualTo("Alice");
    verify(userRepository).save(any(UserEntity.class));
  }

  @Test
  void listsUsers() {
    UserEntity user = new UserEntity();
    user.setId(UUID.randomUUID());
    user.setEmail("alice@example.com");
    user.setName("Alice");
    when(userRepository.findAll()).thenReturn(List.of(user));

    var responses = userService.findAll();

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).email()).isEqualTo("alice@example.com");
  }

  @Test
  void failsWhenUserMissing() {
    UUID missingId = UUID.randomUUID();
    when(userRepository.findById(missingId)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> userService.findById(missingId));
  }
}