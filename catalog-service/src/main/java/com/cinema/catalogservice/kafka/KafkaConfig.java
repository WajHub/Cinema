package com.cinema.catalogservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
  private final String catalogEventsTopic;

  public KafkaConfig(@Value("${app.kafka.topics.catalog-events}") String catalogEventsTopic) {
    this.catalogEventsTopic = catalogEventsTopic;
  }

  @Bean
  public NewTopic setupCatalogEventsTopic() {
    return TopicBuilder.name(catalogEventsTopic)
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
        .build();
  }
}
