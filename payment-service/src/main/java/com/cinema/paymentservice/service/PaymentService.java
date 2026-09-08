package com.cinema.paymentservice.service;

import com.cinema.paymentservice.dto.CreatePaymentRequest;
import com.cinema.paymentservice.dto.CreatePaymentResponse;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.PaymentStatusHistoryEntity;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

  private static final String CURRENCY = "PLN";
  private static final PaymentStatus IN_PROGRESS = PaymentStatus.IN_PROGRESS;

  private final PaymentRepository paymentRepository;
  private final PaymentStatusHistoryRepository statusHistoryRepository;
  private final ObjectMapper objectMapper;

  @Value("${stripe.session-expiration-minutes:30}")
  private long expirationMinutes;

  @Value("${stripe.success-url:http://localhost:8084/api/v1/payments/success?bookingId={BOOKING_ID}}")
  private String successUrl;

  @Value("${stripe.cancel-url:http://localhost:8084/api/v1/payments/cancel?bookingId={BOOKING_ID}}")
  private String cancelUrl;

  public CreatePaymentResponse createCheckoutSession(CreatePaymentRequest request) {
    long amountInCents = request.totalPrice().multiply(BigDecimal.valueOf(100)).longValueExact();
    long effectiveMinutes = Math.max(30, expirationMinutes);
    long expiresAtEpochSeconds = Instant.now().plus(effectiveMinutes, ChronoUnit.MINUTES).plusSeconds(60).getEpochSecond();

    SessionCreateParams params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)
        .setSuccessUrl(successUrl.replace("{BOOKING_ID}", request.bookingId().toString()))
        .setCancelUrl(cancelUrl.replace("{BOOKING_ID}", request.bookingId().toString()))
        .setExpiresAt(expiresAtEpochSeconds)
        .putMetadata("bookingId", request.bookingId().toString())
        .putMetadata("catalogSessionId", request.catalogSessionId().toString())
        .addLineItem(
            SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                    SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(CURRENCY.toLowerCase())
                        .setUnitAmount(amountInCents)
                        .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Cinema Ticket Reservation")
                                .setDescription("Booking ID: " + request.bookingId())
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .build();

    Session session;
    try {
      session = Session.create(params);
    } catch (StripeException exception) {
      throw new IllegalStateException("Failed to create Stripe Checkout Session: " + exception.getMessage(), exception);
    }

    PaymentEntity payment = new PaymentEntity();
    payment.setBookingId(request.bookingId());
    payment.setCatalogSessionId(request.catalogSessionId());
    payment.setCatalogSeatIds(serializeSeatIds(request.catalogSeatIds()));
    payment.setTotalPrice(request.totalPrice());
    payment.setCurrency(CURRENCY);
    payment.setCurrentStatus(IN_PROGRESS);
    payment.setStripeCheckoutSessionId(session.getId());
    payment.setStartedAt(OffsetDateTime.now());

    PaymentEntity savedPayment = paymentRepository.save(payment);
    saveStatusHistory(savedPayment, IN_PROGRESS);

    return new CreatePaymentResponse(
        savedPayment.getId(),
        savedPayment.getBookingId(),
        session.getId(),
        session.getUrl()
    );
  }

  private void saveStatusHistory(PaymentEntity payment, PaymentStatus status) {
    PaymentStatusHistoryEntity history = new PaymentStatusHistoryEntity();
    history.setPayment(payment);
    history.setStatus(status);
    statusHistoryRepository.save(history);
  }

  private String serializeSeatIds(List<UUID> seatIds) {
    try {
      return objectMapper.writeValueAsString(seatIds.stream()
          .map(UUID::toString)
          .toList());
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to serialize seat IDs", exception);
    }
  }
}
