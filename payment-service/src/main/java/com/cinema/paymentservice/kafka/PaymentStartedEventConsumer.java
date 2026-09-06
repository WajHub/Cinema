package com.cinema.paymentservice.kafka;

import com.cinema.kafka.event.PaymentCompletedEventPayload;
import com.cinema.kafka.event.PaymentStartedEvent;
import com.cinema.paymentservice.entity.OutboxEventEntity;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.PaymentStatusHistoryEntity;
import com.cinema.paymentservice.repository.OutboxEventRepository;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentStartedEventConsumer {

  private static final PaymentStatus IN_PROGRESS = PaymentStatus.IN_PROGRESS;
  private static final PaymentStatus COMPLETED = PaymentStatus.COMPLETED;
  private static final String CURRENCY = "PLN";

  private final PaymentRepository paymentRepository;
  private final PaymentStatusHistoryRepository statusHistoryRepository;
  private final ObjectMapper objectMapper;
  private final OutboxEventRepository outboxEventRepository;

  @KafkaListener(topics = "${app.kafka.topics.payment-started}")
  @Transactional
  public void consume(PaymentStartedEvent event) {
    var eventPayload = event.getPayload();
    UUID bookingId = UUID.fromString(eventPayload.getBookingId()
        .toString());
    if (paymentRepository.existsByBookingId(bookingId)) {
      return;
    }

    PaymentEntity payment = new PaymentEntity();
    payment.setBookingId(bookingId);
    payment.setCatalogSessionId(UUID.fromString(eventPayload.getCatalogSessionId()
        .toString()));
    payment.setCatalogSeatIds(writeSeatIds(eventPayload.getCatalogSeatIds()));
    payment.setTotalPrice(new java.math.BigDecimal(eventPayload.getTotalPrice()
        .toString()));
    payment.setCurrency(CURRENCY);
    payment.setCurrentStatus(IN_PROGRESS);
    payment.setStripeCheckoutSessionId(UUID.randomUUID()
        .toString());
    payment.setStartedAt(OffsetDateTime.parse(eventPayload.getCreatedAt()
        .toString()));

    PaymentEntity savedPayment = paymentRepository.save(payment);
    saveStatusHistory(savedPayment, IN_PROGRESS);

    savedPayment.setCurrentStatus(COMPLETED);
    paymentRepository.save(savedPayment);
    saveStatusHistory(savedPayment, COMPLETED);
    persistCompletedEvent(savedPayment);
  }

  private void persistCompletedEvent(PaymentEntity payment) {
    try {
      PaymentCompletedEventPayload payload = PaymentCompletedEventPayload.newBuilder()
          .setPaymentId(payment.getId()
              .toString())
          .setBookingId(payment.getBookingId()
              .toString())
          .setTotalPrice(payment.getTotalPrice()
              .toPlainString())
          .setCurrency(payment.getCurrency())
          .setStripeCheckoutSessionId(payment.getStripeCheckoutSessionId())
          .setCompletedAt(OffsetDateTime.now()
              .toString())
          .setStatus(COMPLETED.name()
              .toLowerCase())
          .build();
      OutboxEventEntity event = new OutboxEventEntity();
      event.setAggregateType("payment");
      event.setAggregateId(payment.getBookingId());
      event.setType("PaymentCompletedEvent");
      event.setPayload(objectMapper.writeValueAsString(payload));
      outboxEventRepository.save(event);
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to persist PaymentCompletedEvent to outbox", exception);
    }
  }

  private void saveStatusHistory(PaymentEntity payment, PaymentStatus status) {
    PaymentStatusHistoryEntity history = new PaymentStatusHistoryEntity();
    history.setPayment(payment);
    history.setStatus(status);
    statusHistoryRepository.save(history);
  }

  private String writeSeatIds(List<String> seatIds) {
    try {
      return objectMapper.writeValueAsString(seatIds.stream()
          .map(CharSequence::toString)
          .toList());
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to serialize catalog seat IDs", exception);
    }
  }
}
