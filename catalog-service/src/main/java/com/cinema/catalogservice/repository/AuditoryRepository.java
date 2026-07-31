package com.cinema.catalogservice.repository;

import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.entity.AuditoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoryRepository extends JpaRepository<AuditoryEntity, UUID> {

	List<AuditoryEntity> findAllByCinema_Id(UUID cinemaId);
}
