package com.cinema.bookingservice.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record MovieSessionResponse( //
    UUID id, //
    UUID catalogSessionId, //
    String movieTitle, //
    OffsetDateTime startsAt, //
    OffsetDateTime endsAt, //
    List<SessionSeatResponse> seats //
) {
}
