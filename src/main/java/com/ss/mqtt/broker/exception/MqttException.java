package com.ss.mqtt.broker.exception;

public class MqttException extends RuntimeException {

  public MqttException() {}

  public MqttException(String message) {
    super(message);
  }

  public MqttException(Throwable cause) {
    super(cause);
  }
}
