package com.cinema.catalogservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CinemaRequest(
    @NotBlank String name,
    @NotBlank String address,
    @NotBlank String city,
    boolean active) {
}