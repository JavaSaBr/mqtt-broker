package com.ss.mqtt.broker.handler.client;

import com.ss.mqtt.broker.network.client.ExternalMqttClient;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.mqtt.broker.service.MqttSessionService;
import com.ss.mqtt.broker.service.SubscriptionService;

public class DefaultMqttClientReleaseHandler extends AbstractMqttClientReleaseHandler<ExternalMqttClient> {

    public DefaultMqttClientReleaseHandler(
        ClientIdRegistry clientIdRegistry,
        MqttSessionService sessionService,
        SubscriptionService subscriptionService
    ) {
        super(clientIdRegistry, sessionService, subscriptionService);
    }
}
