package com.cinema.catalogservice.repository;

import java.util.UUID;
import com.cinema.catalogservice.entity.SessionMovieHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionMovieHistoryRepository extends JpaRepository<SessionMovieHistoryEntity, UUID> {
}
