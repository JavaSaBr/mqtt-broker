package com.ss.mqtt.broker.model.impl;

import com.ss.mqtt.broker.model.MqttSession.UnsafeMqttSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString
@RequiredArgsConstructor
public class DefaultMqttSession implements UnsafeMqttSession {

    private final @NotNull String clientId;

    private volatile @Getter @Setter long expirationTime = -1;

    @Override
    public @NotNull String getClientId() {
        return clientId;
    }
}
