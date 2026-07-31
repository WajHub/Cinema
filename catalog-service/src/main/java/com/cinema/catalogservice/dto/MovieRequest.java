package com.cinema.catalogservice.dto;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MovieRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String posterUrl,
    @NotEmpty List<@NotBlank String> genres,
    @NotNull @Positive Integer durationMinutes,
    @NotBlank String language,
    @NotBlank String ageRating,
    @NotNull LocalDate releaseDate,
    boolean active) {
}