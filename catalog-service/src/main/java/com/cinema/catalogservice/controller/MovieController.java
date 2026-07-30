package com.cinema.catalogservice.controller;

import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.dto.MovieRequest;
import com.cinema.catalogservice.dto.MovieResponse;
import com.cinema.catalogservice.service.MovieService;
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
@RequestMapping("/api/movies")
public class MovieController {

  private final MovieService movieService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MovieResponse create(@Valid @RequestBody MovieRequest request) {
    return movieService.create(request);
  }

  @GetMapping
  public List<MovieResponse> list() {
    return movieService.findAll();
  }

  @GetMapping("/{id}")
  public MovieResponse get(@PathVariable UUID id) {
    return movieService.findById(id);
  }

  @PutMapping("/{id}")
  public MovieResponse update(@PathVariable UUID id, @Valid @RequestBody MovieRequest request) {
    return movieService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    movieService.delete(id);
  }
}