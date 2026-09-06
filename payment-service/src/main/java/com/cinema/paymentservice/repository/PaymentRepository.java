package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.PaymentEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByBookingId(UUID bookingId);

    List<PaymentEntity> findByCurrentStatusAndStartedAtBefore(String currentStatus, OffsetDateTime startedAt);
}