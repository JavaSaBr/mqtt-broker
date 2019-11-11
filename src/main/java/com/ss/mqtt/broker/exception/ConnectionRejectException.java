package com.ss.mqtt.broker.exception;

import com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ConnectionRejectException extends MqttException {

    private final @Getter @NotNull ConnectAckReasonCode reasonCode;

    public ConnectionRejectException(@NotNull ConnectAckReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public ConnectionRejectException(@NotNull Throwable cause, @NotNull ConnectAckReasonCode reasonCode) {
        super(cause);
        this.reasonCode = reasonCode;
    }
}
