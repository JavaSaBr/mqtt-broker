package com.ss.mqtt.broker.exception;

import org.jetbrains.annotations.NotNull;

public class CredentialsSourceException extends RuntimeException {

    public CredentialsSourceException(@NotNull String message) {
        super(message);
    }

    public CredentialsSourceException(@NotNull Throwable cause) {
        super(cause);
    }
}
