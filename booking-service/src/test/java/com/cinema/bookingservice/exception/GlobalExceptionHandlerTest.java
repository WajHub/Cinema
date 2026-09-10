package com.cinema.bookingservice.exception;

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
    when(request.getRequestURI()).thenReturn("/api/v1/bookings");
  }

  @Test
  @DisplayName("Should handle ResponseStatusException and return ProblemDetail")
  void handleResponseStatusException() {
    var ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");

    ProblemDetail problem = handler.handleResponseStatusException(ex, request);

    assertThat(problem).isNotNull();
    assertThat(problem.getStatus()).isEqualTo(404);
    assertThat(problem.getDetail()).isEqualTo("Booking not found");
    assertThat(problem.getInstance().toString()).isEqualTo("/api/v1/bookings");
  }

  @Test
  @DisplayName("Should handle DoubleBokingException and return 409 Conflict ProblemDetail")
  void handleDoubleBookingException() {
    var ex = new DoubleBokingException("One or more seats were reserved");

    ProblemDetail problem = handler.handleDoubleBookingException(ex, request);

    assertThat(problem).isNotNull();
    assertThat(problem.getStatus()).isEqualTo(409);
    assertThat(problem.getTitle()).isEqualTo("Booking Conflict");
    assertThat(problem.getDetail()).isEqualTo("One or more seats were reserved");
  }

  @Test
  @DisplayName("Should handle MethodArgumentNotValidException and include field validation errors")
  void handleMethodArgumentNotValidException() throws Exception {
    var parameter = new MethodParameter(
        GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class), 0);
    var bindingResult = new BeanPropertyBindingResult(new Object(), "target");
    bindingResult.addError(new FieldError("target", "seatIds", "must not be empty"));
    var ex = new MethodArgumentNotValidException(parameter, bindingResult);

    ProblemDetail problem = handler.handleMethodArgumentNotValidException(ex, request);

    assertThat(problem).isNotNull();
    assertThat(problem.getStatus()).isEqualTo(400);
    assertThat(problem.getTitle()).isEqualTo("Bad Request");
    assertThat(problem.getProperties()).containsKey("validationErrors");
    @SuppressWarnings("unchecked")
    Map<String, String> fieldErrors = (Map<String, String>) problem.getProperties().get("validationErrors");
    assertThat(fieldErrors).containsEntry("seatIds", "must not be empty");
  }

  @Test
  @DisplayName("Should handle IllegalArgumentException and return 400 Bad Request ProblemDetail")
  void handleIllegalArgumentException() {
    var ex = new IllegalArgumentException("Invalid argument specified");

    ProblemDetail problem = handler.handleIllegalArgumentException(ex, request);

    assertThat(problem).isNotNull();
    assertThat(problem.getStatus()).isEqualTo(400);
    assertThat(problem.getDetail()).isEqualTo("Invalid argument specified");
  }

  @Test
  @DisplayName("Should handle general Exception and return 500 Internal Server Error ProblemDetail")
  void handleGeneralException() {
    var ex = new RuntimeException("Database error");

    ProblemDetail problem = handler.handleGeneralException(ex, request);

    assertThat(problem).isNotNull();
    assertThat(problem.getStatus()).isEqualTo(500);
    assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
    assertThat(problem.getDetail()).isEqualTo("An unexpected internal server error occurred");
  }
}
