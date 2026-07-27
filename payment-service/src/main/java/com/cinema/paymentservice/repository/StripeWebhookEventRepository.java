package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.StripeWebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEventEntity, Long> {
}