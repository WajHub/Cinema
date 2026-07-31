package com.cinema.catalogservice.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MovieResponse(UUID id, String title, String description, String posterUrl,
    List<String> genres, Integer durationMinutes, String language, String ageRating,
    LocalDate releaseDate, boolean active) {
}