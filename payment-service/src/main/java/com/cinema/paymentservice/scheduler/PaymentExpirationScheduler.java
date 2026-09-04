package com.cinema.paymentservice.scheduler;

import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.PaymentStatusHistoryEntity;
import com.cinema.paymentservice.entity.OutboxEventEntity;
import com.cinema.paymentservice.kafka.event.PaymentCancelledEventPayload;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.cinema.paymentservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

  private static final PaymentStatus IN_PROGRESS = PaymentStatus.IN_PROGRESS;
  private static final PaymentStatus CANCELLED = PaymentStatus.CANCELLED;

  private final PaymentRepository paymentRepository;
  private final PaymentStatusHistoryRepository statusHistoryRepository;
  private final ObjectMapper objectMapper;
  private final OutboxEventRepository outboxEventRepository;

  @Value("${app.payment.expiration-minutes:15}")
  private long expirationMinutes;

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

      persistCancelledEvent(payment);
  }
}

private void persistCancelledEvent(PaymentEntity payment) {
  try {
      PaymentCancelledEventPayload payload = new PaymentCancelledEventPayload(
              payment.getId(), payment.getBookingId(), payment.getCatalogSessionId(),
              readSeatIds(payment.getCatalogSeatIds()), payment.getTotalPrice().toPlainString(),
              payment.getCurrency(), payment.getStripeCheckoutSessionId(), OffsetDateTime.now().toString(),
          "Payment expired after " + expirationMinutes + " minutes", CANCELLED.name().toLowerCase());
      OutboxEventEntity event = new OutboxEventEntity();
      event.setAggregateType("payment");
      event.setAggregateId(payment.getBookingId());
      event.setType("PaymentCancelledEvent");
      event.setPayload(objectMapper.writeValueAsString(payload));
      outboxEventRepository.save(event);
  } catch (Exception exception) {
      throw new IllegalStateException("Failed to persist PaymentCancelledEvent to outbox", exception);
    }
  }

  private List<String> readSeatIds(String serializedSeatIds) {
    try {
      List<String> seatIds = new ArrayList<>();
      for (var seatId : objectMapper.readTree(serializedSeatIds)) {
        seatIds.add(seatId.asText());
      }
      return seatIds;
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to deserialize catalog seat IDs", exception);
    }
  }
}
