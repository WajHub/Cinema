package com.cinema.paymentservice.controller;

import com.cinema.paymentservice.dto.CreatePaymentRequest;
import com.cinema.paymentservice.dto.CreatePaymentResponse;
import com.cinema.paymentservice.dto.StripeConnectionStatusResponse;
import com.cinema.paymentservice.service.PaymentService;
import com.cinema.paymentservice.service.StripeHealthCheckService;
import com.cinema.paymentservice.service.StripeWebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

  private final PaymentService paymentService;
  private final StripeHealthCheckService stripeHealthCheckService;
  private final StripeWebhookService stripeWebhookService;

  @PostMapping("/checkout-session")
  public ResponseEntity<CreatePaymentResponse> createCheckoutSession(
      @Valid @RequestBody CreatePaymentRequest request) {
    CreatePaymentResponse response = paymentService.createCheckoutSession(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/stripe/webhook")
  public ResponseEntity<Void> handleStripeWebhook(
      @RequestBody String payload,
      @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
    stripeWebhookService.processWebhook(payload, sigHeader);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/stripe/status")
  public ResponseEntity<StripeConnectionStatusResponse> getStripeStatus() {
    StripeConnectionStatusResponse response = stripeHealthCheckService.checkConnection();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/success")
  public ResponseEntity<String> paymentSuccess() {
    return ResponseEntity.ok("SUCCESS");
  }

  @GetMapping("/cancel")
  public ResponseEntity<String> paymentCancel() {
    return ResponseEntity.ok("CANCELLED");
  }
}
