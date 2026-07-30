package com.cinema.catalogservice.controller;

import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.dto.CinemaRequest;
import com.cinema.catalogservice.dto.CinemaResponse;
import com.cinema.catalogservice.service.CinemaService;
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
@RequestMapping("/api/cinemas")
public class CinemaController {

  private final CinemaService cinemaService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CinemaResponse create(@Valid @RequestBody CinemaRequest request) {
    return cinemaService.create(request);
  }

  @GetMapping
  public List<CinemaResponse> list() {
    return cinemaService.findAll();
  }

  @GetMapping("/{id}")
  public CinemaResponse get(@PathVariable UUID id) {
    return cinemaService.findById(id);
  }

  @PutMapping("/{id}")
  public CinemaResponse update(@PathVariable UUID id, @Valid @RequestBody CinemaRequest request) {
    return cinemaService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    cinemaService.delete(id);
  }
}