package com.cinema.paymentservice.service;

import com.cinema.paymentservice.entity.PaymentEntity;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeRefundService {

  public Refund processRefund(PaymentEntity payment) {
    try {
      String paymentIntentId = payment.getStripePaymentIntentId();

      if (paymentIntentId == null || paymentIntentId.isBlank()) {
        throw new IllegalStateException("Cannot process refund: No Stripe PaymentIntent ID found for payment " + payment.getId());
      }

      long amountInGrosze = payment.getTotalPrice()
          .multiply(BigDecimal.valueOf(100))
          .longValueExact();

      RefundCreateParams params = RefundCreateParams.builder()
          .setPaymentIntent(paymentIntentId)
          .setAmount(amountInGrosze)
          .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
          .putMetadata("bookingId", payment.getBookingId()
              .toString())
          .putMetadata("paymentId", payment.getId()
              .toString())
          .build();

      Refund stripeRefund = Refund.create(params);
      log.info("Successfully created Stripe refund {} for payment {}", stripeRefund.getId(), payment.getId());
      return stripeRefund;
    } catch (StripeException exception) {
      log.error("Stripe refund creation failed for payment {}: {}", payment.getId(), exception.getMessage(), exception);
      throw new IllegalStateException("Failed to process Stripe refund: " + exception.getMessage(), exception);
    }
  }
}
