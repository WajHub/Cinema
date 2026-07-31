package com.cinema.catalogservice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionResponse(UUID id, UUID auditoryId, String auditoryName, UUID movieId,
    String movieTitle, OffsetDateTime startsAt, OffsetDateTime endsAt, BigDecimal basePrice,
    String status) {
}