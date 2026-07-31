package com.cinema.catalogservice.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CinemaResponse(UUID id, String name, String address, String city, boolean active,
    OffsetDateTime createdAt) {
}