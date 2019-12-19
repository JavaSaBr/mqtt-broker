package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import org.jetbrains.annotations.NotNull;

abstract class AbstractPublishOutHandler implements PublishOutHandler {

    @Override
    public @NotNull ActionResult handle(@NotNull PublishInPacket packet, @NotNull Subscriber subscriber) {

        var client = subscriber.getMqttClient();
        var session = client.getSession();

        // if session is null it means this client was already closed
        if (session != null) {
            return handleImpl(packet, subscriber, client, session);
        } else {
            return ActionResult.EMPTY;
        }
    }

    protected abstract @NotNull ActionResult handleImpl(
        @NotNull PublishInPacket packet,
        @NotNull Subscriber subscriber,
        @NotNull MqttClient client,
        @NotNull MqttSession session
    );

    protected abstract @NotNull QoS getQoS();

    void sendPublish(
        @NotNull MqttClient client,
        @NotNull PublishInPacket packet,
        int packetId,
        boolean duplicate
    ) {

        var packetOutFactory = client.getPacketOutFactory();
        client.send(packetOutFactory.newPublish(
            packetId,
            getQoS(),
            packet.isRetained(),
            duplicate,
            packet.getTopicName().toString(),
            MqttPropertyConstants.TOPIC_ALIAS_NOT_SET,
            packet.getPayload(),
            packet.isPayloadFormatIndicator(),
            packet.getResponseTopic(),
            packet.getCorrelationData(),
            packet.getUserProperties()
        ));
    }
}
