package com.ss.mqtt.broker.network.client.impl;

import com.ss.mqtt.broker.network.MqttConnection;
import org.jetbrains.annotations.NotNull;

public class DeviceMqttClient extends AbstractMqttClient {

    public DeviceMqttClient(@NotNull MqttConnection connection) {
        super(connection);
    }
}
