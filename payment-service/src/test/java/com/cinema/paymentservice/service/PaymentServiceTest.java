package com.cinema.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cinema.paymentservice.dto.CreatePaymentRequest;
import com.cinema.paymentservice.dto.CreatePaymentResponse;
import com.cinema.paymentservice.entity.PaymentEntity;
import com.cinema.paymentservice.entity.PaymentStatus;
import com.cinema.paymentservice.entity.PaymentStatusHistoryEntity;
import com.cinema.paymentservice.repository.PaymentRepository;
import com.cinema.paymentservice.repository.PaymentStatusHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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

  @Test
  void createCheckoutSession_savesPaymentAndReturnsCheckoutUrl() {
    UUID bookingId = UUID.randomUUID();
    UUID sessionCatalogId = UUID.randomUUID();
    UUID seatId = UUID.randomUUID();
    CreatePaymentRequest request = new CreatePaymentRequest(
        bookingId,
        sessionCatalogId,
        List.of(seatId),
        new BigDecimal("45.00")
    );

    when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
      PaymentEntity entity = invocation.getArgument(0);
      entity.setId(UUID.randomUUID());
      return entity;
    });

    CreatePaymentResponse response = paymentService.createCheckoutSession(request);

    assertThat(response).isNotNull();
    assertThat(response.bookingId()).isEqualTo(bookingId);
    assertThat(response.stripeCheckoutSessionId()).startsWith("cs_test_");
    assertThat(response.checkoutUrl()).contains("https://checkout.stripe.com/c/pay/");

    verify(paymentRepository).save(any(PaymentEntity.class));
    verify(statusHistoryRepository).save(any(PaymentStatusHistoryEntity.class));
  }
}
