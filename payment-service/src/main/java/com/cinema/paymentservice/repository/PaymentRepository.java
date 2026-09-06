package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

  boolean existsByBookingId(UUID bookingId);

  List<PaymentEntity> findByCurrentStatusAndStartedAtBefore(PaymentStatus currentStatus, OffsetDateTime startedAt);

  Optional<PaymentEntity> findByBookingIdAndCurrentStatus(UUID bookingId, PaymentStatus status);

  Optional<PaymentEntity> findByBookingId(UUID bookingId);
}
