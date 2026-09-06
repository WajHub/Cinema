package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.OutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

  @Query(value = """
      select *
      from outbox_event
      where processed_at is null
      order by created_at
      limit :limit
      for update skip locked
      """, nativeQuery = true)
  List<OutboxEventEntity> lockNextBatch(@Param("limit") int limit);
}
