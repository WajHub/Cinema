package com.cinema.bookingservice.exception;

public class DoubleBokingException extends RuntimeException {
  public DoubleBokingException(String message) {
    super(message);
  }

  public DoubleBokingException(String message, Throwable cause) {
    super(message, cause);
  }
}
