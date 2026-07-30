package com.cinema.bookingservice.controller;

import java.util.List;
import java.util.UUID;
import com.cinema.bookingservice.dto.UserRequest;
import com.cinema.bookingservice.dto.UserResponse;
import com.cinema.bookingservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse create(@Valid @RequestBody UserRequest request) {
    return userService.create(request);
  }

  @GetMapping
  public List<UserResponse> list() {
    return userService.findAll();
  }

  @GetMapping("/{id}")
  public UserResponse get(@PathVariable UUID id) {
    return userService.findById(id);
  }

  @PutMapping("/{id}")
  public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
    return userService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    userService.delete(id);
  }
}