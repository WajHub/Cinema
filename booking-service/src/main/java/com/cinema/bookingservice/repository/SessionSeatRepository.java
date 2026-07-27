package com.cinema.bookingservice.repository;

import java.util.UUID;
import com.cinema.bookingservice.entity.SessionSeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionSeatRepository extends JpaRepository<SessionSeatEntity, UUID> {
}