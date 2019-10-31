package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.PublishAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.PublishingService;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class SimplePublishingService implements PublishingService {

    private final @NotNull SubscriptionService subscriptionService;

    private static @NotNull PublishAckReasonCode send(
        @NotNull MqttClient mqttClient,
        @NotNull PublishInPacket publish
    ) {
        var connection = mqttClient.getConnection();
        var packetOutFactory = mqttClient.getPacketOutFactory();
        connection.send(packetOutFactory.newPublish(
            mqttClient,
            publish.getPacketId(),
            publish.getQos(),
            publish.isRetained(),
            publish.isDuplicate(),
            publish.getTopicName(),
            publish.getTopicAlias(),
            publish.getPayload(),
            publish.isPayloadFormatIndicator(),
            null, //publish.getResponseTopic(),
            null,
            publish.getUserProperties()
        ));
        return PublishAckReasonCode.SUCCESS;
    }

    @Override
    public @NotNull PublishAckReasonCode publish(@NotNull MqttClient mqttClient, @NotNull PublishInPacket publish) {
        var subscribers = subscriptionService.getSubscribers(publish.getTopicName());
        // TODO choose correct PublishAckReasonCode
        return subscribers.stream()
            .map(targetMqttClient -> send(mqttClient, publish))
            .findFirst()
            .orElse(PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS);
    }
}
