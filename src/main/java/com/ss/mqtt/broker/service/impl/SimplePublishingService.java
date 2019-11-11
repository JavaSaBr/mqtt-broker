package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.reason.code.PublishAckReasonCode;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.PublishingService;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Simple publishing service
 */
@RequiredArgsConstructor
public class SimplePublishingService implements PublishingService {

    private final @NotNull SubscriptionService subscriptionService;

    private static @NotNull PublishAckReasonCode send(
        @NotNull Subscriber subscriber,
        @NotNull PublishInPacket publish
    ) {

        var mqttClient = subscriber.getMqttClient();
        mqttClient.send(mqttClient.getPacketOutFactory().newPublish(
            mqttClient,
            publish.getPacketId(),
            publish.getQos(),
            publish.isRetained(),
            publish.isDuplicate(),
            publish.getTopicName(),
            publish.getTopicAlias(),
            publish.getPayload(),
            publish.isPayloadFormatIndicator(),
            publish.getResponseTopic(),
            publish.getCorrelationData(),
            publish.getUserProperties()
        ));

        // TODO this reason code only for QoS 0
        return PublishAckReasonCode.SUCCESS;
    }

    @Override
    public @NotNull PublishAckReasonCode publish(@NotNull PublishInPacket publish) {

        var subscribers = subscriptionService.getSubscribers(publish.getTopicName());

        if (subscribers.isEmpty()) {
            return PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS;
        }

        var success = subscribers.stream()
            .map(subscriber -> send(subscriber, publish))
            .allMatch(ackReasonCode -> ackReasonCode.equals(PublishAckReasonCode.SUCCESS));

        return success ? PublishAckReasonCode.SUCCESS : PublishAckReasonCode.UNSPECIFIED_ERROR;
    }
}
