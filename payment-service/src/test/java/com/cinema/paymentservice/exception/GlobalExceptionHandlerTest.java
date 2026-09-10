package com.cinema.paymentservice.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private HttpServletRequest request;

  private static void dummyMethod(String param) {}

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/v1/payments/stripe/webhook");
  }

  @Test
  @DisplayName("Should handle ResponseStatusException and return ProblemDetail")
  void handleResponseStatusException() {
    var ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");

    ProblemDetail problem = handler.handleResponseStatusException(ex, request);

    assertThat(problem).isNotNull();
    assertThat(problem.getStatus()).isEqualTo(404);
    assertThat(problem.getDetail()).isEqualTo("Payment not found");
    assertThat(problem.getInstance().toString()).isEqualTo("/api/v1/payments/stripe/webhook");
  }

  @Test
  @DisplayName("Should handle MethodArgumentNotValidException and include field validation errors")
  void handleMethodArgumentNotValidException() throws Exception {
    var parameter = new MethodParameter(
        GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class), 0);
    var bindingResult = new BeanPropertyBindingResult(new Object(), "target");
    bindingResult.addError(new FieldError("target", "bookingId", "must not be null"));
    var ex = new MethodArgumentNotValidException(parameter, bindingResult);

    ProblemDetail problem = handler.handleMethodArgumentNotValidException(ex, request);

    assertThat(problem).isNotNull();
    assertThat(problem.getStatus()).isEqualTo(400);
    assertThat(problem.getTitle()).isEqualTo("Bad Request");
    assertThat(problem.getProperties()).containsKey("validationErrors");
    @SuppressWarnings("unchecked")
    Map<String, String> fieldErrors = (Map<String, String>) problem.getProperties().get("validationErrors");
    assertThat(fieldErrors).containsEntry("bookingId", "must not be null");
  }

  @Test
  @DisplayName("Should handle IllegalArgumentException for missing Stripe signature")
  void handleIllegalArgumentException() {
    var ex = new IllegalArgumentException("Missing Stripe-Signature header");

    ProblemDetail problem = handler.handleIllegalArgumentException(ex, request);

    assertThat(problem).isNotNull();
    assertThat(problem.getStatus()).isEqualTo(400);
    assertThat(problem.getDetail()).isEqualTo("Missing Stripe-Signature header");
  }

  @Test
  @DisplayName("Should handle general Exception and return 500 Internal Server Error ProblemDetail")
  void handleGeneralException() {
    var ex = new RuntimeException("Stripe API connection failed");

    ProblemDetail problem = handler.handleGeneralException(ex, request);

    assertThat(problem).isNotNull();
    assertThat(problem.getStatus()).isEqualTo(500);
    assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
    assertThat(problem.getDetail()).isEqualTo("An unexpected internal server error occurred");
  }
}
