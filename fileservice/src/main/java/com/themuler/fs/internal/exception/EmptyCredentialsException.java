package com.themuler.fs.internal.exception;

public class EmptyCredentialsException extends Exception {
  private final String message;

  public EmptyCredentialsException(String message) {
    super(message);
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
