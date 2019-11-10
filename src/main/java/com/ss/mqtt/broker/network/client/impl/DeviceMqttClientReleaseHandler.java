package com.ss.mqtt.broker.network.client.impl;

import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.mqtt.broker.service.MqttSessionService;
import org.jetbrains.annotations.NotNull;

public class DeviceMqttClientReleaseHandler extends AbstractMqttClientReleaseHandler<DeviceMqttClient> {

    public DeviceMqttClientReleaseHandler(
        @NotNull ClientIdRegistry clientIdRegistry,
        @NotNull MqttSessionService sessionService
    ) {
        super(clientIdRegistry, sessionService);
    }
}
