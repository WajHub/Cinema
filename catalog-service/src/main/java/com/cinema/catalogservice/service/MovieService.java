package com.cinema.catalogservice.service;

import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.dto.MovieRequest;
import com.cinema.catalogservice.dto.MovieResponse;
import com.cinema.catalogservice.entity.MovieEntity;
import com.cinema.catalogservice.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieService {

  private final MovieRepository movieRepository;

  public MovieResponse create(MovieRequest request) {
    MovieEntity movie = new MovieEntity();
    apply(movie, request);
    return toResponse(movieRepository.save(movie));
  }

  @Transactional(readOnly = true)
  public List<MovieResponse> findAll() {
    return movieRepository.findAll().stream().map(this::toResponse).collect(java.util.stream.Collectors.toList());
  }

  @Transactional(readOnly = true)
  public MovieResponse findById(UUID id) {
    return toResponse(findEntity(id));
  }

  public MovieResponse update(UUID id, MovieRequest request) {
    MovieEntity movie = findEntity(id);
    apply(movie, request);
    return toResponse(movieRepository.save(movie));
  }

  public void delete(UUID id) {
    movieRepository.delete(findEntity(id));
  }

  private void apply(MovieEntity movie, MovieRequest request) {
    movie.setTitle(request.title());
    movie.setDescription(request.description());
    movie.setPosterUrl(request.posterUrl());
    movie.setGenres(request.genres().toArray(String[]::new));
    movie.setDurationMinutes(request.durationMinutes());
    movie.setLanguage(request.language());
    movie.setAgeRating(request.ageRating());
    movie.setReleaseDate(request.releaseDate());
    movie.setActive(request.active());
  }

  private MovieEntity findEntity(UUID id) {
    return movieRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
  }

  private MovieResponse toResponse(MovieEntity movie) {
    return new MovieResponse(movie.getId(), movie.getTitle(), movie.getDescription(),
        movie.getPosterUrl(), List.of(movie.getGenres()), movie.getDurationMinutes(),
        movie.getLanguage(), movie.getAgeRating(), movie.getReleaseDate(), movie.isActive());
  }
}