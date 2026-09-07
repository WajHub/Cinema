package com.cinema.bookingservice.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentServiceClient {

  private final RestClient restClient;

  public PaymentServiceClient(@Value("${app.services.payment-service.url:http://localhost:8084}") String paymentServiceUrl) {
    this.restClient = RestClient.builder()
        .baseUrl(paymentServiceUrl)
        .build();
  }

  public CreatePaymentResponse createCheckoutSession(CreatePaymentRequest request) {
    return restClient.post()
        .uri("/api/v1/payments/checkout-session")
        .body(request)
        .retrieve()
        .body(CreatePaymentResponse.class);
  }

  public record CreatePaymentRequest(UUID bookingId, UUID catalogSessionId, List<UUID> catalogSeatIds, BigDecimal totalPrice) {
  }

  public record CreatePaymentResponse(UUID paymentId, UUID bookingId, String stripeCheckoutSessionId, String checkoutUrl) {
  }
}
