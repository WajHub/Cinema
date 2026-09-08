package com.cinema.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.cinema.paymentservice.entity.PaymentEntity;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StripeRefundServiceTest {

  @InjectMocks
  private StripeRefundService stripeRefundService;

  @Test
  void processRefund_createsStripeRefundInGrosze() {
    PaymentEntity payment = new PaymentEntity();
    payment.setId(UUID.randomUUID());
    payment.setBookingId(UUID.randomUUID());
    payment.setStripePaymentIntentId("pi_test_mock123");
    payment.setTotalPrice(new BigDecimal("45.00"));

    Refund mockRefund = mock(Refund.class);
    when(mockRefund.getId()).thenReturn("re_test_mock123");

    try (MockedStatic<Refund> mockedStatic = mockStatic(Refund.class)) {
      mockedStatic.when(() -> Refund.create(any(RefundCreateParams.class))).thenReturn(mockRefund);

      Refund refund = stripeRefundService.processRefund(payment);

      assertThat(refund).isNotNull();
      assertThat(refund.getId()).isEqualTo("re_test_mock123");
    }
  }
}
