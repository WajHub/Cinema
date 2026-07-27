package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.PaymentStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistoryEntity, Long> {
}