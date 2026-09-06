package com.cinema.paymentservice.repository;

import com.cinema.paymentservice.entity.StripeWebhookEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEventEntity, UUID> {
}