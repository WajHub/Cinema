package com.cinema.paymentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment")
public class PaymentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "stripe_payment_intent_id", unique = true)
  private String stripePaymentIntentId;

  @Column(name = "stripe_checkout_session_id", nullable = false, unique = true)
  private String stripeCheckoutSessionId;

  @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
  private BigDecimal totalPrice;

  @Column(nullable = false, length = 16)
  private String currency;

  @Column(name = "current_status", nullable = false, length = 32)
  private String currentStatus;

  @Column(name = "booking_id", nullable = false)
  private UUID bookingId;

  @Column(name = "catalog_session_id", nullable = false)
  private UUID catalogSessionId;

  @Column(name = "catalog_seat_ids", nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String catalogSeatIds;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;
}
