package com.cinema.bookingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "spring.kafka.admin.auto-create", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {
  private final String refundStartedTopic;

  public KafkaConfig(@Value("${app.kafka.topics.refund-started}") String refundStartedTopic) {
    this.refundStartedTopic = refundStartedTopic;
  }

  @Bean
  public NewTopic setupRefundStartedTopic() {
    return TopicBuilder.name(refundStartedTopic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
        .build();
  }
}
