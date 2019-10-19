package com.ss.mqtt.broker.exception;

import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ConnectionRejectException extends RuntimeException {

    private final @NotNull ConnectAckReasonCode reasonCode;
}
