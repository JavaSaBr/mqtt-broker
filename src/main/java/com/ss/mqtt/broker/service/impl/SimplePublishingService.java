package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.PublishAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.factory.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.PublishingService;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class SimplePublishingService implements PublishingService {

    private final SubscriptionService subscriptionService;

    private static PublishAckReasonCode send(@NotNull MqttClient mqttClient, @NotNull PublishInPacket publish) {
        var connection = mqttClient.getConnection();
        var packetOutFactory = mqttClient.getPacketOutFactory();
        connection.send(packetOutFactory.newPublish(
            mqttClient,
            publish.getPacketId(),
            publish.getQos().ordinal(),
            publish.isRetained(),
            publish.isDuplicate(),
            publish.getTopicName(),
            publish.getPayload()
        ));
        return PublishAckReasonCode.SUCCESSFUL;
    }

    @Override
    public Array<PublishAckReasonCode> publish(@NotNull MqttClient mqttClient, @NotNull PublishInPacket publish) {
        var subscribers = subscriptionService.getSubscribers(publish.getTopicName());
        // TODO choose correct PublishAckReasonCode
        return subscribers.stream()
            .map(targetMqttClient -> send(mqttClient, publish))
            .collect(ArrayCollectors.toArray(PublishAckReasonCode.class));
    }
}
