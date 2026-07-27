package com.cinema.catalogservice.repository;

import java.util.UUID;
import com.cinema.catalogservice.entity.MovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<MovieEntity, UUID> {
}
