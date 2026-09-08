package com.cinema.paymentservice.service;

import com.cinema.paymentservice.dto.StripeConnectionStatusResponse;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StripeHealthCheckService {

  public StripeConnectionStatusResponse checkConnection() {
    try {
      Account account = Account.retrieve();
      String businessName = account.getBusinessProfile() != null ? account.getBusinessProfile().getName() : null;
      log.info("Successfully connected to Stripe API. Account ID: {}", account.getId());

      return new StripeConnectionStatusResponse(
          true,
          "CONNECTED",
          account.getId(),
          businessName,
          "Successfully authenticated and connected to Stripe API"
      );
    } catch (AuthenticationException exception) {
      log.error("Stripe authentication failed: {}", exception.getMessage());
      return new StripeConnectionStatusResponse(
          false,
          "AUTHENTICATION_FAILED",
          null,
          null,
          "Invalid or missing Stripe Secret Key: " + exception.getMessage()
      );
    } catch (StripeException exception) {
      log.error("Stripe API error during health check: {}", exception.getMessage());
      return new StripeConnectionStatusResponse(
          false,
          "ERROR",
          null,
          null,
          "Stripe API error: " + exception.getMessage()
      );
    }
  }
}
