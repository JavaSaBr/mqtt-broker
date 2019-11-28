package com.ss.mqtt.broker.exception;

import org.jetbrains.annotations.NotNull;

public class InconsistentSubscriptionStateException extends RuntimeException {

    public InconsistentSubscriptionStateException(@NotNull String message) {
        super(message);
    }

    public InconsistentSubscriptionStateException(@NotNull Throwable cause) {
        super(cause);
    }
}
