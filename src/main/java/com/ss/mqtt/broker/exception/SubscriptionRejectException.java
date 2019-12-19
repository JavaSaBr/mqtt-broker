package com.ss.mqtt.broker.exception;

import com.ss.mqtt.broker.model.reason.code.DisconnectReasonCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class SubscriptionRejectException extends MqttException {

    private final @Getter @NotNull DisconnectReasonCode reasonCode;

    public SubscriptionRejectException(@NotNull DisconnectReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public SubscriptionRejectException(@NotNull Throwable cause, @NotNull DisconnectReasonCode reasonCode) {
        super(cause);
        this.reasonCode = reasonCode;
    }
}
