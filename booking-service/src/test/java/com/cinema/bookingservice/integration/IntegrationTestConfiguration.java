package com.cinema.bookingservice.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cinema.bookingservice.client.PaymentServiceClient;
import com.cinema.bookingservice.repository.BookingRepository;
import com.cinema.bookingservice.repository.MovieSessionRepository;
import com.cinema.bookingservice.repository.OutboxEventRepository;
import com.cinema.bookingservice.repository.SessionSeatRepository;
import com.cinema.bookingservice.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class IntegrationTestConfiguration {

  @Autowired
  protected WebTestClient webTestClient;

  @Autowired
  protected KafkaTemplate<String, Object> kafkaTemplate;

  @MockitoBean
  protected PaymentServiceClient paymentServiceClient;

  static final KafkaContainer kafka;

  static {
    kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));
    kafka.start();
  }

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
  protected OutboxEventRepository outboxEventRepository;

  @Autowired
  protected UserRepository userRepository;

  @BeforeEach
  void cleanDatabase() {
    outboxEventRepository.deleteAllInBatch();
    sessionSeatRepository.deleteAllInBatch();
    bookingRepository.deleteAllInBatch();
    movieSessionRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();

    when(paymentServiceClient.createCheckoutSession(any()))
        .thenAnswer(invocation -> {
          PaymentServiceClient.CreatePaymentRequest req = invocation.getArgument(0);
          return new PaymentServiceClient.CreatePaymentResponse(
              UUID.randomUUID(), req.bookingId(), "cs_test_mock", "https://checkout.stripe.com/c/pay/cs_test_mock");
        });
  }
}
