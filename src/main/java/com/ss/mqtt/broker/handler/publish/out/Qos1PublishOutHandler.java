package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class Qos1PublishOutHandler extends AbstractPublishOutHandler implements MqttSession.PendingPacketHandler {

    @Override
    public boolean handle(@NotNull PublishInPacket packet, @NotNull Subscriber subscriber) {

        var client = subscriber.getMqttClient();
        var session = client.getSession();

        // it means this client was already closed
        if (session == null) {
            return false;
        }

        var packetId = session.nextPacketId();
        session.registerPendingPublish(packet, this, packetId);

        var packetOutFactory = client.getPacketOutFactory();
        client.send(packetOutFactory.newPublish(
            client,
            packetId,
            QoS.AT_LEAST_ONCE_DELIVERY,
            packet.isRetained(),
            false,
            packet.getTopicName().toString(),
            MqttPropertyConstants.TOPIC_ALIAS_NOT_SET,
            packet.getPayload(),
            packet.isPayloadFormatIndicator(),
            packet.getResponseTopic(),
            packet.getCorrelationData(),
            packet.getUserProperties()
        ));
        return true;
    }

    @Override
    public boolean handleResponse(@NotNull MqttClient client, @NotNull HasPacketId response) {
        return true;
    }

    @Override
    public void retryAsync(@NotNull MqttClient client, @NotNull PublishInPacket packet, int packetId) {
        var packetOutFactory = client.getPacketOutFactory();
        client.send(packetOutFactory.newPublish(
            client,
            packetId,
            QoS.AT_LEAST_ONCE_DELIVERY,
            packet.isRetained(),
            true,
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
