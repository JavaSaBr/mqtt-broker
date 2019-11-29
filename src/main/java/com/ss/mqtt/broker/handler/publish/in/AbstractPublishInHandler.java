package com.ss.mqtt.broker.handler.publish.in;

import com.ss.mqtt.broker.handler.publish.out.PublishOutHandler;
import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
abstract class AbstractPublishInHandler implements PublishInHandler {

    protected final @NotNull SubscriptionService subscriptionService;
    protected final @NotNull PublishOutHandler[] publishOutHandlers;

    public void handle(@NotNull MqttClient client, @NotNull PublishInPacket packet) {
        handleResult(client, packet, subscriptionService.forEachTopicSubscriber(
            packet.getTopicName(),
            packet,
            this::sendToSubscriber
        ));
    }

    private @NotNull ActionResult sendToSubscriber(
        @NotNull Subscriber subscriber,
        @NotNull PublishInPacket packet
    ) {
        return publishOutHandler(subscriber.getQos()).handle(packet, subscriber);
    }

    private @NotNull PublishOutHandler publishOutHandler(@NotNull QoS qos) {
        return publishOutHandlers[qos.ordinal()];
    }

    protected void handleResult(
        @NotNull MqttClient client,
        @NotNull PublishInPacket packet,
        @NotNull ActionResult result
    ) {
        // nothing to do
    }
}
