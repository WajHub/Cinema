package com.cinema.paymentservice.kafka;

import com.cinema.kafka.event.RefundCompletedEventPayload;
import com.cinema.kafka.event.RefundStartedEvent;
import com.cinema.paymentservice.entity.OutboxEventEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.RefundEntity;
import com.cinema.paymentservice.entity.RefundStatus;
import com.cinema.paymentservice.repository.OutboxEventRepository;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.RefundRepository;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatusHistoryEntity;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.cinema.paymentservice.service.StripeRefundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Refund;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RefundStartedEventConsumer {

  private static final RefundStatus IN_PROGRESS = RefundStatus.IN_PROGRESS;
  private static final RefundStatus COMPLETED = RefundStatus.COMPLETED;

  private final RefundRepository refundRepository;
  private final PaymentRepository paymentRepository;
  private final PaymentStatusHistoryRepository statusHistoryRepository;
  private final ObjectMapper objectMapper;
  private final OutboxEventRepository outboxEventRepository;
  private final StripeRefundService stripeRefundService;

  @KafkaListener(topics = "${app.kafka.topics.refund-started}")
  @Transactional
  public void consume(RefundStartedEvent event) {
    var eventPayload = event.getPayload();
    UUID bookingId = UUID.fromString(eventPayload.getBookingId().toString());
    var payment = paymentRepository.findByBookingId(bookingId).orElseThrow();

    RefundEntity refund = new RefundEntity();
    refund.setPayment(payment);
    refund.setStatus(IN_PROGRESS);
    refund.setTotalPrice(payment.getTotalPrice());
    refund.setCurrency(eventPayload.getCurrency());
    refundRepository.save(refund);

    payment.setCurrentStatus(PaymentStatus.REFUND_PENDING);
    paymentRepository.save(payment);

    Refund stripeRefund = stripeRefundService.processRefund(payment);

    refund.setStripeRefundId(stripeRefund.getId());
    refund.setStatus(COMPLETED);
    RefundEntity savedRefund = refundRepository.save(refund);

    payment.setCurrentStatus(PaymentStatus.REFUNDED);
    paymentRepository.save(payment);
    saveStatusHistory(payment, PaymentStatus.REFUNDED);

    persistCompletedEvent(savedRefund);
  }

  private void saveStatusHistory(PaymentEntity payment, PaymentStatus status) {
    PaymentStatusHistoryEntity history = new PaymentStatusHistoryEntity();
    history.setPayment(payment);
    history.setStatus(status);
    statusHistoryRepository.save(history);
  }

  private void persistCompletedEvent(RefundEntity refund) {
    try {
      RefundCompletedEventPayload payload = RefundCompletedEventPayload.newBuilder()
          .setBookingId(refund.getPayment()
              .getBookingId()
              .toString())
          .setTotalPrice(refund.getTotalPrice()
              .toPlainString())
          .setCreatedAt(OffsetDateTime.now()
              .toString())
          .build();
      OutboxEventEntity event = new OutboxEventEntity();
      event.setAggregateType("refund");
      event.setAggregateId(refund.getId());
      event.setType("RefundCompletedEvent");
      event.setPayload(objectMapper.writeValueAsString(payload));
      outboxEventRepository.save(event);
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to persist RefundCompletedEvent to outbox", exception);
    }
  }
}

