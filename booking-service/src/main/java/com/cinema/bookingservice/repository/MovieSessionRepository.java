package com.cinema.bookingservice.repository;

import com.cinema.bookingservice.entity.MovieSessionEntity;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieSessionRepository extends JpaRepository<MovieSessionEntity, UUID> {

    Optional<MovieSessionEntity> findByCatalogSessionId(UUID catalogSessionId);

    @Modifying
    @Query(value = """
            insert into movie_session (id, catalog_session_id, movie_title, starts_at, ends_at)
            values (:id, :catalogSessionId, :movieTitle, :startsAt, :endsAt)
            on conflict (catalog_session_id) do update set
             movie_title = excluded.movie_title,
             starts_at = excluded.starts_at,
             ends_at = excluded.ends_at
            """, nativeQuery = true)
    int upsertSession(@Param("id") UUID id,
            @Param("catalogSessionId") UUID catalogSessionId,
            @Param("movieTitle") String movieTitle,
            @Param("startsAt") OffsetDateTime startsAt,
            @Param("endsAt") OffsetDateTime endsAt);
}