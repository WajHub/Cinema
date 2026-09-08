package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

  List<PaymentEntity> findByCurrentStatusAndStartedAtBefore(PaymentStatus currentStatus, OffsetDateTime startedAt);

  Optional<PaymentEntity> findByBookingId(UUID bookingId);

  Optional<PaymentEntity> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
}
