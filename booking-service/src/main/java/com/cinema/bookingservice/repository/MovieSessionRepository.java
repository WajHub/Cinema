package com.cinema.bookingservice.repository;

import java.util.UUID;
import com.cinema.bookingservice.entity.MovieSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieSessionRepository extends JpaRepository<MovieSessionEntity, UUID> {
}