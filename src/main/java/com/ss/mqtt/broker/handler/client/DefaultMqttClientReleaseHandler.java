package com.ss.mqtt.broker.handler.client;

import com.ss.mqtt.broker.network.client.DeviceMqttClient;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.mqtt.broker.service.MqttSessionService;
import com.ss.mqtt.broker.service.SubscriptionService;
import org.jetbrains.annotations.NotNull;

public class DefaultMqttClientReleaseHandler extends AbstractMqttClientReleaseHandler<DeviceMqttClient> {

    public DefaultMqttClientReleaseHandler(
        @NotNull ClientIdRegistry clientIdRegistry,
        @NotNull MqttSessionService sessionService,
        @NotNull SubscriptionService subscriptionService
    ) {
        super(clientIdRegistry, sessionService, subscriptionService);
    }
}
