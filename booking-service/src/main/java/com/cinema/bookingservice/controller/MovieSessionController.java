package com.cinema.bookingservice.controller;

import com.cinema.bookingservice.dto.MovieSessionResponse;
import com.cinema.bookingservice.service.MovieSessionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movie-sessions")
public class MovieSessionController {

  private final MovieSessionService movieSessionService;

  @GetMapping
  public List<MovieSessionResponse> list() {
    return movieSessionService.findAll();
  }
}
