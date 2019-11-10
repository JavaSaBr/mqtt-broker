package com.ss.mqtt.broker.network.client.impl;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.client.MqttClientReleaseHandler;
import org.jetbrains.annotations.NotNull;

public class DeviceMqttClient extends AbstractMqttClient {

    public DeviceMqttClient(@NotNull MqttConnection connection, @NotNull MqttClientReleaseHandler releaseHandler) {
        super(connection, releaseHandler);
    }
}
