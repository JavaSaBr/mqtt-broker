package com.ss.mqtt.broker.handler.publish.in;

import com.ss.mqtt.broker.handler.publish.out.PublishOutHandler;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import org.jetbrains.annotations.NotNull;

public class Qos2PublishInHandler extends AbstractPublishInHandler {

    public Qos2PublishInHandler(
        @NotNull SubscriptionService subscriptionService,
        @NotNull PublishOutHandler[] publishOutHandlers
    ) {
        super(subscriptionService, publishOutHandlers);
    }

    @Override
    public void handle(@NotNull MqttClient client, @NotNull PublishInPacket packet) {
        throw new UnsupportedOperationException();
    }
}
