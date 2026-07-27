package com.cinema.catalogservice.repository;

import java.util.UUID;
import com.cinema.catalogservice.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {
}
