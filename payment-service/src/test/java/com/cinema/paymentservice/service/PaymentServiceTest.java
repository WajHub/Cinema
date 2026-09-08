package com.cinema.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cinema.paymentservice.dto.CreatePaymentRequest;
import com.cinema.paymentservice.dto.CreatePaymentResponse;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private PaymentStatusHistoryRepository statusHistoryRepository;

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  private PaymentService paymentService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(paymentService, "successUrl", "http://localhost:8084/api/v1/payments/success?bookingId={BOOKING_ID}");
    ReflectionTestUtils.setField(paymentService, "cancelUrl", "http://localhost:8084/api/v1/payments/cancel?bookingId={BOOKING_ID}");
    ReflectionTestUtils.setField(paymentService, "expirationMinutes", 30L);
  }

  @Test
  void createCheckoutSession_savesPaymentAndReturnsStripeUrl() {
    UUID bookingId = UUID.randomUUID();
    UUID sessionCatalogId = UUID.randomUUID();
    UUID seatId = UUID.randomUUID();
    CreatePaymentRequest request = new CreatePaymentRequest(
        bookingId,
        sessionCatalogId,
        List.of(seatId),
        new BigDecimal("45.00")
    );

    Session mockSession = mock(Session.class);
    when(mockSession.getId()).thenReturn("cs_test_mock123");
    when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_mock123");

    when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
      PaymentEntity entity = invocation.getArgument(0);
      entity.setId(UUID.randomUUID());
      return entity;
    });

    try (MockedStatic<Session> mockedStatic = mockStatic(Session.class)) {
      mockedStatic.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

      CreatePaymentResponse response = paymentService.createCheckoutSession(request);

      assertThat(response).isNotNull();
      assertThat(response.bookingId()).isEqualTo(bookingId);
      assertThat(response.stripeCheckoutSessionId()).isEqualTo("cs_test_mock123");
      assertThat(response.checkoutUrl()).isEqualTo("https://checkout.stripe.com/pay/cs_test_mock123");

      verify(paymentRepository).save(any(PaymentEntity.class));
      verify(statusHistoryRepository).save(any());
    }
  }
}
