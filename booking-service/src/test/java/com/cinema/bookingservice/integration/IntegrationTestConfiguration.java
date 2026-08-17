package com.cinema.bookingservice.integration;

import com.cinema.bookingservice.repository.BookingRepository;
import com.cinema.bookingservice.repository.MovieSessionRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.bookingservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.kafka.core.KafkaTemplate;
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

  @Autowired
  protected KafkaTemplate<String, Object> kafkaTemplate;

  @Container
  static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @Autowired
  protected MovieSessionRepository movieSessionRepository;

  @Autowired
  protected SessionSeatRepository sessionSeatRepository;

  @Autowired
  protected BookingRepository bookingRepository;

  @Autowired
  protected UserRepository userRepository;

  @BeforeEach
  void cleanDatabase() {
    sessionSeatRepository.deleteAllInBatch();
    bookingRepository.deleteAllInBatch();
    movieSessionRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
  }
}
