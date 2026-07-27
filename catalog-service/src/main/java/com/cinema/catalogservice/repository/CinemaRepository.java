package com.cinema.catalogservice.repository;

import java.util.UUID;
import com.cinema.catalogservice.entity.CinemaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CinemaRepository extends JpaRepository<CinemaEntity, UUID> {
}
