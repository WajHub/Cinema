package com.cinema.catalogservice.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class KafkaTestSupport {

  protected static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
      DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

  static {
    KAFKA_CONTAINER.start();
  }

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    registry.add("spring.kafka.properties.schema.registry.url", () -> "mock://catalog-test");
    registry.add("app.kafka.topics.catalog-events", () -> "test.catalog-events");
    registry.add("app.outbox.poll-delay-ms", () -> "600000");
  }
}