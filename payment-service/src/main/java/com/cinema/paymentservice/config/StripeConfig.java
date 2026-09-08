package com.cinema.paymentservice.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

  @Value("${stripe.secret.key:}")
  private String stripeSecretKey;

  @PostConstruct
  public void init() {
    if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
      Stripe.apiKey = stripeSecretKey;
    }
  }
}
