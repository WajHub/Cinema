package com.cinema.bookingservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
    @Email @NotBlank String email,
    @NotBlank String name) {
}