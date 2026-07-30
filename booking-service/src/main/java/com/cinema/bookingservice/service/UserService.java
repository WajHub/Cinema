package com.cinema.bookingservice.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.cinema.bookingservice.dto.UserRequest;
import com.cinema.bookingservice.dto.UserResponse;
import com.cinema.bookingservice.entity.UserEntity;
import com.cinema.bookingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;

  public UserResponse create(UserRequest request) {
    UserEntity user = new UserEntity();
    user.setEmail(request.email());
    user.setName(request.name());
    return toResponse(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  public List<UserResponse> findAll() {
    return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public UserResponse findById(UUID id) {
    return toResponse(findEntityById(id));
  }

  public UserResponse update(UUID id, UserRequest request) {
    UserEntity user = findEntityById(id);
    user.setEmail(request.email());
    user.setName(request.name());
    return toResponse(userRepository.save(user));
  }

  public void delete(UUID id) {
    userRepository.delete(findEntityById(id));
  }

  private UserEntity findEntityById(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  private UserResponse toResponse(UserEntity user) {
    return new UserResponse(user.getId(), user.getEmail(), user.getName());
  }
}