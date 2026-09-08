package com.cinema.paymentservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cinema.paymentservice.dto.CreatePaymentRequest;
import com.cinema.paymentservice.dto.CreatePaymentResponse;
import com.cinema.paymentservice.dto.StripeConnectionStatusResponse;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.service.PaymentService;
import com.cinema.paymentservice.service.StripeHealthCheckService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PaymentControllerIntegrationTest extends IntegrationTestConfiguration {

  @MockitoBean
  private PaymentService paymentService;

  @MockitoBean
  private StripeHealthCheckService stripeHealthCheckService;

  @Test
  @DisplayName("Should create checkout session via REST API using WebTestClient")
  void createCheckoutSession_success() {
    UUID paymentId = UUID.randomUUID();
    UUID bookingId = UUID.randomUUID();
    UUID sessionCatalogId = UUID.randomUUID();
    UUID seatId = UUID.randomUUID();
    CreatePaymentRequest request = new CreatePaymentRequest(
        bookingId,
        sessionCatalogId,
        List.of(seatId),
        new BigDecimal("45.00")
    );

    CreatePaymentResponse expectedResponse = new CreatePaymentResponse(
        paymentId,
        bookingId,
        "cs_test_mock123",
        "https://checkout.stripe.com/pay/cs_test_mock123"
    );

    when(paymentService.createCheckoutSession(any())).thenReturn(expectedResponse);

    webTestClient.post()
        .uri("/api/v1/payments/checkout-session")
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(CreatePaymentResponse.class)
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.paymentId()).isEqualTo(paymentId);
          assertThat(response.bookingId()).isEqualTo(bookingId);
          assertThat(response.stripeCheckoutSessionId()).isEqualTo("cs_test_mock123");
          assertThat(response.checkoutUrl()).isEqualTo("https://checkout.stripe.com/pay/cs_test_mock123");
        });
  }

  @Test
  @DisplayName("Should process Stripe webhook callback via REST API using WebTestClient")
  void handleStripeWebhook_checkoutSessionCompleted() {
    String eventId = "evt_integration_test123";
    String sessionId = "cs_integration_test456";

    PaymentEntity payment = new PaymentEntity();
    payment.setBookingId(UUID.randomUUID());
    payment.setCatalogSessionId(UUID.randomUUID());
    payment.setCatalogSeatIds("[\"" + UUID.randomUUID() + "\"]");
    payment.setTotalPrice(new BigDecimal("50.00"));
    payment.setCurrency("PLN");
    payment.setCurrentStatus(PaymentStatus.IN_PROGRESS);
    payment.setStripeCheckoutSessionId(sessionId);
    payment.setStartedAt(OffsetDateTime.now());
    payment = paymentRepository.save(payment);

    String payload = """
        {
          "id": "%s",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "object": "checkout.session",
              "id": "%s",
              "payment_intent": "pi_integration_intent789"
            }
          }
        }
        """.formatted(eventId, sessionId);

    webTestClient.post()
        .uri("/api/v1/payments/stripe/webhook")
        .bodyValue(payload)
        .exchange()
        .expectStatus()
        .isOk();

    PaymentEntity updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
    assertThat(updatedPayment.getCurrentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(updatedPayment.getStripePaymentIntentId()).isEqualTo("pi_integration_intent789");

    assertThat(webhookEventRepository.existsByStripeEventId(eventId)).isTrue();
    assertThat(outboxEventRepository.findAll()).isNotEmpty();
  }

  @Test
  @DisplayName("Should return Stripe connection status")
  void getStripeStatus_returnsConnectedStatus() {
    StripeConnectionStatusResponse expectedStatus = new StripeConnectionStatusResponse(
        true,
        "CONNECTED",
        "acct_test123",
        "Cinema Test",
        "Connected"
    );

    when(stripeHealthCheckService.checkConnection()).thenReturn(expectedStatus);

    webTestClient.get()
        .uri("/api/v1/payments/stripe/status")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(StripeConnectionStatusResponse.class)
        .value(response -> {
          assertThat(response).isNotNull();
          assertThat(response.connected()).isTrue();
          assertThat(response.stripeAccountId()).isEqualTo("acct_test123");
        });
  }

  @Test
  @DisplayName("Should return SUCCESS on payment success redirect")
  void paymentSuccess_returnsSuccessText() {
    webTestClient.get()
        .uri("/api/v1/payments/success")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("SUCCESS");
  }

  @Test
  @DisplayName("Should return CANCELLED on payment cancel redirect")
  void paymentCancel_returnsCancelledText() {
    webTestClient.get()
        .uri("/api/v1/payments/cancel")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("CANCELLED");
  }
}
