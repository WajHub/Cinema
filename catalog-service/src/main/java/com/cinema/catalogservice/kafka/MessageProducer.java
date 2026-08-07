package com.cinema.catalogservice.kafka;

@Configuration
public class KafkaConfig {
  @Bean
  public NewTopic cinemaTicketsTopic() {
    return TopicBuilder.name("cinema-tickets-v1")
        .partitions(3)
        .replicas(1)
        .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
        .build();
  }

}
