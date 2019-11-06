package com.ss.mqtt.broker.model;

import org.jetbrains.annotations.NotNull;

public interface MqttSession {

    @NotNull String getClientId();

    /**
     * @return the expiration time in ms or -1 if it should not be expired now.
     */
    long getExpirationTime();
}
