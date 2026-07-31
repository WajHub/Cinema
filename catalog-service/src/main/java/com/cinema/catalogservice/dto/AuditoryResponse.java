package com.cinema.catalogservice.dto;

import java.util.List;
import java.util.UUID;

public record AuditoryResponse(UUID id, UUID cinemaId, String cinemaName, String name,
    Integer capacity, boolean active, List<SeatResponse> seats) {
}