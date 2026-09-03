package com.cinema.paymentservice.scheduler;

import com.cinema.kafka.event.PaymentCancelledEvent;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatusHistoryEntity;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

  private static final String IN_PROGRESS = "in_progress";
  private static final String CANCELLED = "cancelled";

  private final PaymentRepository paymentRepository;
  private final PaymentStatusHistoryRepository statusHistoryRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.payment.expiration-minutes:15}")
  private long expirationMinutes;

  @Value("${app.kafka.topics.payment-cancelled}")
  private String paymentCancelledTopic;

  @Scheduled(fixedDelayString = "${app.payment.expiration-poll-delay-ms:30000}")
  @Transactional
  public void cancelExpiredPayments() {
    OffsetDateTime expirationTime = OffsetDateTime.now().minusMinutes(expirationMinutes);
    List<PaymentEntity> expiredPayments = paymentRepository
        .findByCurrentStatusAndStartedAtBefore(IN_PROGRESS, expirationTime);

    for (PaymentEntity payment : expiredPayments) {
      payment.setCurrentStatus(CANCELLED);
      paymentRepository.save(payment);

      PaymentStatusHistoryEntity history = new PaymentStatusHistoryEntity();
      history.setPayment(payment);
      history.setStatus(CANCELLED);
      statusHistoryRepository.save(history);

      PaymentCancelledEvent cancelledEvent = PaymentCancelledEvent.newBuilder()
          .setPaymentId(payment.getId().toString())
          .setBookingId(payment.getBookingId().toString())
          .setCatalogSessionId(payment.getCatalogSessionId().toString())
          .setCatalogSeatIds(readSeatIds(payment.getCatalogSeatIds()))
          .setTotalPrice(payment.getTotalPrice().toPlainString())
          .setCurrency(payment.getCurrency())
          .setStripeCheckoutSessionId(payment.getStripeCheckoutSessionId())
          .setCancelledAt(OffsetDateTime.now().toString())
          .setReason("Payment expired after " + expirationMinutes + " minutes")
          .setStatus(CANCELLED)
          .build();
      kafkaTemplate.send(paymentCancelledTopic, payment.getBookingId().toString(), cancelledEvent);
    }
  }

  private List<String> readSeatIds(String serializedSeatIds) {
    try {
      List<String> seatIds = new ArrayList<>();
      JsonNode root = objectMapper.readTree(serializedSeatIds);
      for (JsonNode seatId : root) {
        seatIds.add(seatId.asText());
      }
      return seatIds;
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to deserialize catalog seat IDs", exception);
    }
  }
}
