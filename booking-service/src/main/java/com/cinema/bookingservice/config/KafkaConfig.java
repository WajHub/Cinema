package com.cinema.bookingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
  private final String catalogEventsTopic;
  private final String paymentStartedTopic;
  private final String paymentCompletedTopic;
  private final String paymentCancelledTopic;

  public KafkaConfig(@Value("${app.kafka.topics.catalog-events}") String catalogEventsTopic,
      @Value("${app.kafka.topics.payment-started}") String paymentStartedTopic,
      @Value("${app.kafka.topics.payment-completed}") String paymentCompletedTopic,
      @Value("${app.kafka.topics.payment-cancelled}") String paymentCancelledTopic) {
    this.catalogEventsTopic = catalogEventsTopic;
    this.paymentStartedTopic = paymentStartedTopic;
    this.paymentCompletedTopic = paymentCompletedTopic;
    this.paymentCancelledTopic = paymentCancelledTopic;
  }

  @Bean
  public NewTopic setupCatalogEventsTopic() {
    return TopicBuilder.name(catalogEventsTopic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
        .build();
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
    return buildTopic(paymentCompletedTopic);
  }

  @Bean
  public NewTopic setupPaymentCancelledTopic() {
    return buildTopic(paymentCancelledTopic);
  }

  private NewTopic buildTopic(String topic) {
    return TopicBuilder.name(topic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
        .build();
  }
}
