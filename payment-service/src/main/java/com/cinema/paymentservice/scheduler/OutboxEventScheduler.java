package com.cinema.paymentservice.scheduler;

import com.cinema.paymentservice.entity.OutboxEventEntity;
import com.cinema.paymentservice.kafka.OutboxEventProducer;
import com.cinema.paymentservice.repository.OutboxEventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

  private final OutboxEventRepository outboxEventRepository;
  private final List<OutboxEventProducer> producers;

  @Value("${app.outbox.batch-size:50}")
  private int batchSize;

  @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:2000}")
  @Transactional
  public void publishPendingEvents() {
    for (OutboxEventEntity event : outboxEventRepository.lockNextBatch(batchSize)) {
      OutboxEventProducer producer = producers.stream()
          .filter(candidate -> candidate.supports(event.getType()))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("No producer for outbox event type: " + event.getType()));
      producer.publish(event);
      outboxEventRepository.deleteById(event.getId());
    }
  }
}
