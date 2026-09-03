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

  public KafkaConfig(@Value("${app.kafka.topics.catalog-events}") String catalogEventsTopic,
      @Value("${app.kafka.topics.payment-started}") String paymentStartedTopic) {
    this.catalogEventsTopic = catalogEventsTopic;
    this.paymentStartedTopic = paymentStartedTopic;
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
}
