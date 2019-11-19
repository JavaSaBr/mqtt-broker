package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.PacketIdGenerator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class Qos1PublishOutHandler extends AbstractPublishOutHandler {

    private final @NotNull PacketIdGenerator packetIdGenerator;

    @Override
    public void handle(@NotNull PublishInPacket packet, @NotNull Subscriber subscriber) {

        var client = subscriber.getMqttClient();
        var packetOutFactory = client.getPacketOutFactory();
        var publish = packetOutFactory.newPublish(
            client,
            packetIdGenerator.nextPacketId(),
            QoS.AT_LEAST_ONCE_DELIVERY,
            packet.isRetained(),
            packet.isDuplicate(),
            packet.getTopicName(),
            MqttPropertyConstants.TOPIC_ALIAS_NOT_SET,
            packet.getPayload(),
            packet.isPayloadFormatIndicator(),
            packet.getResponseTopic(),
            packet.getCorrelationData(),
            packet.getUserProperties()
        );

        var session = client.getSession();
        session.registerPendingPublish(publish);

        client.send(publish);
    }
}
