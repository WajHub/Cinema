package com.cinema.paymentservice.service;

import com.cinema.paymentservice.dto.CreatePaymentRequest;
import com.cinema.paymentservice.dto.CreatePaymentResponse;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.PaymentStatusHistoryEntity;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

  public CreatePaymentResponse createCheckoutSession(CreatePaymentRequest request) {
    paymentRepository.findByBookingId(request.bookingId())
        .ifPresent(existing -> {
          // If a payment already exists for this booking, we return existing payment details
        });

    String stripeCheckoutSessionId = "cs_test_" + UUID.randomUUID();
    String checkoutUrl = "https://checkout.stripe.com/c/pay/" + stripeCheckoutSessionId;

    PaymentEntity payment = new PaymentEntity();
    payment.setBookingId(request.bookingId());
    payment.setCatalogSessionId(request.catalogSessionId());
    payment.setCatalogSeatIds(serializeSeatIds(request.catalogSeatIds()));
    payment.setTotalPrice(request.totalPrice());
    payment.setCurrency(CURRENCY);
    payment.setCurrentStatus(IN_PROGRESS);
    payment.setStripeCheckoutSessionId(stripeCheckoutSessionId);
    payment.setStartedAt(OffsetDateTime.now());

    PaymentEntity savedPayment = paymentRepository.save(payment);
    saveStatusHistory(savedPayment, IN_PROGRESS);

    return new CreatePaymentResponse(
        savedPayment.getId(),
        savedPayment.getBookingId(),
        stripeCheckoutSessionId,
        checkoutUrl
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
