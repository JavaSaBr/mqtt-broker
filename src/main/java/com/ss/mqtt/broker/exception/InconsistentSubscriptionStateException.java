package com.ss.mqtt.broker.exception;

public class InconsistentSubscriptionStateException extends RuntimeException {

  public InconsistentSubscriptionStateException(String message) {
    super(message);
  }

  public InconsistentSubscriptionStateException(Throwable cause) {
    super(cause);
  }
}
