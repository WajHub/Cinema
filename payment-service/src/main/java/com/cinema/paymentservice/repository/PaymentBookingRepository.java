package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.PaymentBookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentBookingRepository extends JpaRepository<PaymentBookingEntity, Long> {
}