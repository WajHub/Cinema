package com.cinema.paymentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  @Column(nullable = false)
  private Long amount;

  @Column(nullable = false, length = 16)
  private String currency;

  @Column(name = "current_status", nullable = false, length = 32)
  private String currentStatus;
}