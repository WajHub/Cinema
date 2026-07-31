package com.cinema.catalogservice.repository;

import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {

	List<SeatEntity> findAllByAuditory_Id(UUID auditoryId);
}
