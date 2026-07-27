package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.RefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<RefundEntity, Long> {
}