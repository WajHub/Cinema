package com.cinema.catalogservice.controller;

import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.dto.SessionRequest;
import com.cinema.catalogservice.dto.SessionResponse;
import com.cinema.catalogservice.service.SessionService;
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
@RequestMapping("/api/sessions")
public class SessionController {

  private final SessionService sessionService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SessionResponse create(@Valid @RequestBody SessionRequest request) {
    return sessionService.create(request);
  }

  @GetMapping
  public List<SessionResponse> list() {
    return sessionService.findAll();
  }

  @GetMapping("/{id}")
  public SessionResponse get(@PathVariable UUID id) {
    return sessionService.findById(id);
  }

  @PutMapping("/{id}")
  public SessionResponse update(@PathVariable UUID id, @Valid @RequestBody SessionRequest request) {
    return sessionService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    sessionService.delete(id);
  }
}