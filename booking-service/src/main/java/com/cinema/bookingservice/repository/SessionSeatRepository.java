package com.cinema.bookingservice.repository;

import com.cinema.bookingservice.entity.SessionSeatEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionSeatRepository extends JpaRepository<SessionSeatEntity, UUID> {

    Optional<SessionSeatEntity> findBySession_IdAndSeatId(UUID sessionId, UUID seatId);

    @Modifying
    @Query(value = """
            insert into session_seat (id, session_id, booking_id, seat_id, row_label, seat_number, final_price)
            values (:id, :sessionId, null, :seatId, :rowLabel, :seatNumber, :finalPrice)
            on conflict (session_id, seat_id) do update set
             row_label = excluded.row_label,
             seat_number = excluded.seat_number,
             final_price = excluded.final_price,
             booking_id = session_seat.booking_id
            """, nativeQuery = true)
    int upsertSeat(@Param("id") UUID id,
            @Param("sessionId") UUID sessionId,
            @Param("seatId") UUID seatId,
            @Param("rowLabel") String rowLabel,
            @Param("seatNumber") Integer seatNumber,
            @Param("finalPrice") BigDecimal finalPrice);
}