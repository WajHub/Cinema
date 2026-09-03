package com.cinema.bookingservice.repository;

import com.cinema.bookingservice.entity.SessionSeatEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionSeatRepository extends JpaRepository<SessionSeatEntity, UUID> {

  Optional<SessionSeatEntity> findByMovieSession_IdAndCatalogSeatId(UUID sessionId, UUID catalogSeatId);

  List<SessionSeatEntity> findByBooking_Id(UUID bookingId);

  @Modifying
  @Query(value = """
      insert into session_seat (id, movie_session_id, booking_id, catalog_seat_id, row_label, seat_number, final_price, status, version)
      values (:id, :sessionId, null, :catalogSeatId, :rowLabel, :seatNumber, :finalPrice, 'AVAILABLE', 0)
      on conflict (movie_session_id, catalog_seat_id) do update set
       row_label = excluded.row_label,
       seat_number = excluded.seat_number,
       final_price = excluded.final_price,
       booking_id = session_seat.booking_id,
       status = CASE WHEN session_seat.booking_id IS NULL THEN 'AVAILABLE' ELSE session_seat.status END
      """, nativeQuery = true)
  int upsertSeat(@Param("id") UUID id, @Param("sessionId") UUID sessionId, //
      @Param("catalogSeatId") UUID catalogSeatId, //
      @Param("rowLabel") String rowLabel, //
      @Param("seatNumber") Integer seatNumber, //
      @Param("finalPrice") BigDecimal finalPrice);
}
