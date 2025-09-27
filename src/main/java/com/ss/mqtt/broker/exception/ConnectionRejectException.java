package com.ss.mqtt.broker.exception;

import com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode;
import lombok.Getter;

public class ConnectionRejectException extends MqttException {

    private final @Getter ConnectAckReasonCode reasonCode;

    public ConnectionRejectException(ConnectAckReasonCode reasonCode) {
        this.reasonCode = reasonCode;
    }

    public ConnectionRejectException(Throwable cause, ConnectAckReasonCode reasonCode) {
        super(cause);
        this.reasonCode = reasonCode;
    }
}
