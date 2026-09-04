package com.cinema.paymentservice.kafka;

import com.cinema.kafka.event.RefundCompletedEvent;
import com.cinema.kafka.event.RefundStartedEvent;
import com.cinema.paymentservice.entity.OutboxEventEntity;
import com.cinema.paymentservice.entity.RefundEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.RefundStatus;
import com.cinema.paymentservice.repository.OutboxEventRepository;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.RefundRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class RefundStartedEventConsumer {

  private static final RefundStatus IN_PROGRESS = RefundStatus.IN_PROGRESS;
  private static final RefundStatus COMPLETED = RefundStatus.COMPLETED;

  private final RefundRepository refundRepository;
  private final PaymentRepository paymentRepository;
  private final ObjectMapper objectMapper;
  private final OutboxEventRepository outboxEventRepository;

  @KafkaListener(topics = "${app.kafka.topics.refund-started}")
  public void consume(RefundStartedEvent event) {
    UUID bookingId = UUID.fromString(event.getBookingId()
        .toString());
    var payment = paymentRepository.findByBookingIdAndCurrentStatus(bookingId, PaymentStatus.COMPLETED)
        .orElseThrow();
    if (refundRepository.existsByPaymentIdAndStatus(payment.getId(), IN_PROGRESS)) {
      return;
    }
    if (refundRepository.existsByPaymentIdAndStatus(payment.getId(), RefundStatus.COMPLETED)) {
      return;
    }

    RefundEntity refund = new RefundEntity();
    refund.setPayment(payment);
    refund.setStatus(IN_PROGRESS);
    refund.setTotalPrice(payment.getTotalPrice());
    refund.setCurrency(event.getCurrency().toString());
    refundRepository.save(refund);

    payment.setCurrentStatus(PaymentStatus.REFUND_PENDING);
    paymentRepository.save(payment);

    refund.setStatus(COMPLETED);
    RefundEntity savedRefund = refundRepository.save(refund);
    payment.setCurrentStatus(PaymentStatus.REFUNDED);
    paymentRepository.save(payment);
    persistCompletedEvent(savedRefund);
  }

  private void persistCompletedEvent(RefundEntity refund) {
    try {
      RefundCompletedEvent payload = new RefundCompletedEvent(refund.getId()
          .toString(),
          refund.getTotalPrice()
              .toPlainString(),
          OffsetDateTime.now()
              .toString());
      OutboxEventEntity event = new OutboxEventEntity();
      event.setAggregateType("refund");
      // event.setAggregateId();
      event.setType("RefundCompletedEvent");
      event.setPayload(objectMapper.writeValueAsString(payload));
      outboxEventRepository.save(event);
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to persist RefundCompletedEvent to outbox", exception);
    }
  }
}
