package io.spring.api.exception;

public class InvalidAuthenticationException extends RuntimeException {

  public InvalidAuthenticationException() {
    super("invalid email or password");
  }
}
