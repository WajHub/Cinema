package com.cinema.catalogservice.repository;

import java.util.List;
import java.util.UUID;
import com.cinema.catalogservice.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

	List<SessionEntity> findAllByAuditory_Id(UUID auditoryId);

	List<SessionEntity> findAllByMovie_Id(UUID movieId);
}
