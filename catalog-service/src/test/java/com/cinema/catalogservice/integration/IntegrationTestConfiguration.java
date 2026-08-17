package com.cinema.catalogservice.integration;

import com.cinema.catalogservice.repository.AuditoryRepository;
import com.cinema.catalogservice.repository.CinemaRepository;
import com.cinema.catalogservice.repository.MovieRepository;
import com.cinema.catalogservice.repository.OutboxEventRepository;
import com.cinema.catalogservice.repository.SeatRepository;
import com.cinema.catalogservice.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class IntegrationTestConfiguration {

  @Autowired
  protected WebTestClient webTestClient;

  @Container
  static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @Autowired
  protected CinemaRepository cinemaRepository;
  @Autowired
  protected AuditoryRepository auditoryRepository;
  @Autowired
  protected SeatRepository seatRepository;
  @Autowired
  protected MovieRepository movieRepository;
  @Autowired
  protected SessionRepository sessionRepository;
  @Autowired
  protected OutboxEventRepository outboxEventRepository;

  @BeforeEach
  void cleanDatabase() {
    outboxEventRepository.deleteAllInBatch();
    sessionRepository.deleteAllInBatch();
    seatRepository.deleteAllInBatch();
    auditoryRepository.deleteAllInBatch();
    movieRepository.deleteAllInBatch();
    cinemaRepository.deleteAllInBatch();
  }
}
