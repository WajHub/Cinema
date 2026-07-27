package com.cinema.catalogservice.repository;

import java.util.UUID;
import com.cinema.catalogservice.entity.AuditoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoryRepository extends JpaRepository<AuditoryEntity, UUID> {
}
