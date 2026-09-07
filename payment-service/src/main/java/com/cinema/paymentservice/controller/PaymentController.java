package com.cinema.paymentservice.controller;

import com.cinema.paymentservice.dto.CreatePaymentRequest;
import com.cinema.paymentservice.dto.CreatePaymentResponse;
import com.cinema.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/checkout-session")
  public ResponseEntity<CreatePaymentResponse> createCheckoutSession(
      @Valid @RequestBody CreatePaymentRequest request) {
    CreatePaymentResponse response = paymentService.createCheckoutSession(request);
    return ResponseEntity.ok(response);
  }
}
