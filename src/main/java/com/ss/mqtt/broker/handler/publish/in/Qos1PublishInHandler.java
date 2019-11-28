package com.ss.mqtt.broker.handler.publish.in;

import com.ss.mqtt.broker.handler.publish.out.PublishOutHandler;
import com.ss.mqtt.broker.model.reason.code.PublishAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import org.jetbrains.annotations.NotNull;

public class Qos1PublishInHandler extends AbstractPublishInHandler {

    public Qos1PublishInHandler(
        @NotNull SubscriptionService subscriptionService,
        @NotNull PublishOutHandler[] publishOutHandlers
    ) {
        super(subscriptionService, publishOutHandlers);
    }

    @Override
    public void handle(@NotNull MqttClient client, @NotNull PublishInPacket packet) {

        var subscribers = subscriptionService.getSubscribers(packet.getTopicName());

        for (var subscriber : subscribers) {
            publishOutHandler(subscriber.getQos()).handle(packet, subscriber);
        }

        var reasonCode = subscribers.isEmpty() ?
            PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS : PublishAckReasonCode.SUCCESS;

        var ackPacket = client.getPacketOutFactory().newPublishAck(
            packet.getPacketId(),
            reasonCode
        );

        client.send(ackPacket);
    }
}
