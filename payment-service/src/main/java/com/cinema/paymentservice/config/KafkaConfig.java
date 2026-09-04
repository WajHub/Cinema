package com.cinema.paymentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class KafkaConfig {

  private final String paymentStartedTopic;
  private final String paymentCompletedTopic;
  private final String paymentCancelledTopic;
  private final String refundCompletedTopic;

  public KafkaConfig(@Value("${app.kafka.topics.payment-started}") String paymentStartedTopic,
      @Value("${app.kafka.topics.payment-completed}") String paymentCompletedTopic,
      @Value("${app.kafka.topics.payment-cancelled}") String paymentCancelledTopic,
      @Value("${app.kafka.topics.refund-completed}") String refundCompletedTopic) {
    this.paymentStartedTopic = paymentStartedTopic;
    this.paymentCompletedTopic = paymentCompletedTopic;
    this.paymentCancelledTopic = paymentCancelledTopic;
    this.refundCompletedTopic = refundCompletedTopic;
  }

  @Bean
  public NewTopic setupPaymentStartedTopic() {
    return TopicBuilder.name(paymentStartedTopic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
        .build();
  }

  @Bean
  public NewTopic setupPaymentCompletedTopic() {
    return TopicBuilder.name(paymentCompletedTopic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
        .build();
  }

  @Bean
  public NewTopic setupPaymentCancelledTopic() {
    return TopicBuilder.name(paymentCancelledTopic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
        .build();
  }

  @Bean
  public NewTopic setupRefundCompletedTopic() {
    return TopicBuilder.name(refundCompletedTopic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
        .build();
  }
}
