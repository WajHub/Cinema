package com.cinema.paymentservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ProblemDetail handleResponseStatusException(
      ResponseStatusException ex, HttpServletRequest request) {
    log.warn("ResponseStatusException on {}: {}", request.getRequestURI(), ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    log.warn("MethodArgumentNotValidException on {}: {}", request.getRequestURI(), ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Validation failed for request body");
    problem.setTitle("Bad Request");
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("timestamp", Instant.now());

    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    problem.setProperty("validationErrors", fieldErrors);
    return problem;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {
    log.warn("ConstraintViolationException on {}: {}", request.getRequestURI(), ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setTitle("Constraint Violation");
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    log.warn("HttpMessageNotReadableException on {}: {}", request.getRequestURI(), ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Malformed JSON request or unreadable payload");
    problem.setTitle("Bad Request");
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    log.warn("MethodArgumentTypeMismatchException on {}: {}", request.getRequestURI(), ex.getMessage());
    String detail = String.format("Parameter '%s' has invalid value '%s'", ex.getName(), ex.getValue());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    problem.setTitle("Bad Request");
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ProblemDetail handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    log.warn("HttpRequestMethodNotSupportedException on {}: {}", request.getRequestURI(), ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    problem.setTitle("Method Not Allowed");
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {
    log.warn("IllegalArgumentException on {}: {}", request.getRequestURI(), ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setTitle("Bad Request");
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleIllegalStateException(
      IllegalStateException ex, HttpServletRequest request) {
    log.warn("IllegalStateException on {}: {}", request.getRequestURI(), ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setTitle("Illegal State");
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneralException(
      Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception on {}: ", request.getRequestURI(), ex);
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred");
    problem.setTitle("Internal Server Error");
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }
}
