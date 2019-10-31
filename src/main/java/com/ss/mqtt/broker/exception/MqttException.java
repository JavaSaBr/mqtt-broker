package com.ss.mqtt.broker.exception;

import org.jetbrains.annotations.NotNull;

public class MqttException extends RuntimeException {

    public MqttException() {
    }

    public MqttException(@NotNull String message) {
        super(message);
    }

    public MqttException(@NotNull Throwable cause) {
        super(cause);
    }
}
