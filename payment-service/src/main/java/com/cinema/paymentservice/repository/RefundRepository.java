package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.RefundEntity;
import com.cinema.paymentservice.entity.RefundStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<RefundEntity, UUID> {

  boolean existsByPaymentIdAndStatus(UUID paymentId, RefundStatus status);
}
