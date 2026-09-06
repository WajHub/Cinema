package com.cinema.catalogservice.kafka.event;

import java.util.UUID;

public record SessionChangedEventSeatPayload(
    UUID seatId,
    String rowLabel,
    Integer seatNumber,
    String price) {}
