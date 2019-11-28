package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import org.jetbrains.annotations.NotNull;

abstract class AbstractPublishOutHandler implements PublishOutHandler {

    @Override
    public void handle(@NotNull PublishInPacket packet, @NotNull Subscriber subscriber) {

        var client = subscriber.getMqttClient();
        var session = client.getSession();

        // if session is null it means this client was already closed
        if (session != null) {
            handleImpl(packet, subscriber, client, session);
        }
    }

    protected abstract void handleImpl(
        @NotNull PublishInPacket packet,
        @NotNull Subscriber subscriber,
        @NotNull MqttClient client,
        @NotNull MqttSession session
    );

    protected abstract @NotNull QoS getQoS();

    protected void sendPublish(
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
            packet.getTopicName(),
            MqttPropertyConstants.TOPIC_ALIAS_NOT_SET,
            packet.getPayload(),
            packet.isPayloadFormatIndicator(),
            packet.getResponseTopic(),
            packet.getCorrelationData(),
            packet.getUserProperties()
        ));
    }
}
