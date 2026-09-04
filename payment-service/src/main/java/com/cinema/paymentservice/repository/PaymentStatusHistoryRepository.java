package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.PaymentStatusHistoryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistoryEntity, UUID> {
}