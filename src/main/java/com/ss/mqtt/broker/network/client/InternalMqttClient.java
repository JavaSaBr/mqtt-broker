package com.ss.mqtt.broker.network.client;

import com.ss.mqtt.broker.handler.client.MqttClientReleaseHandler;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.util.DebugUtils;
import org.jetbrains.annotations.NotNull;

public class InternalMqttClient extends AbstractMqttClient {

    static {
        DebugUtils.registerIncludedFields("clientId");
    }

    public InternalMqttClient(@NotNull MqttConnection connection, @NotNull MqttClientReleaseHandler releaseHandler) {
        super(connection, releaseHandler);
    }
}
