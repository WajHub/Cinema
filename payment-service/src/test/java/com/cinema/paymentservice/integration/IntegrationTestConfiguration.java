package com.cinema.paymentservice.integration;

import com.cinema.paymentservice.repository.OutboxEventRepository;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.cinema.paymentservice.repository.RefundRepository;
import com.cinema.paymentservice.repository.StripeWebhookEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public abstract class IntegrationTestConfiguration {

  @Autowired
  protected WebTestClient webTestClient;

  @Autowired
  protected KafkaTemplate<String, Object> kafkaTemplate;

  @Autowired
  protected PaymentRepository paymentRepository;

  @Autowired
  protected PaymentStatusHistoryRepository statusHistoryRepository;

  @Autowired
  protected RefundRepository refundRepository;

  @Autowired
  protected OutboxEventRepository outboxEventRepository;

  @Autowired
  protected StripeWebhookEventRepository webhookEventRepository;

  @Autowired
  protected ObjectMapper objectMapper;

  static final KafkaContainer kafka;

  static {
    kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));
    kafka.start();
  }

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add("stripe.webhook.secret", () -> "");
  }

  @BeforeEach
  void cleanDatabase() {
    outboxEventRepository.deleteAllInBatch();
    refundRepository.deleteAllInBatch();
    statusHistoryRepository.deleteAllInBatch();
    paymentRepository.deleteAllInBatch();
    webhookEventRepository.deleteAllInBatch();
  }
}
