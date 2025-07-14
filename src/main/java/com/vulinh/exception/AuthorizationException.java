package com.vulinh.exception;

import java.io.Serial;

public class AuthorizationException extends RuntimeException {

  @Serial private static final long serialVersionUID = -4977646741872972264L;

  public AuthorizationException(String message) {
    super(message);
  }
}
