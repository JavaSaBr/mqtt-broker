package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.PublishAckReasonCode;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.PublishingService;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Simple publishing service
 */
@RequiredArgsConstructor
public class SimplePublishingService implements PublishingService {

    private static final Map<QoS, BiFunction<MqttClient, PublishInPacket, Boolean>> SEND_BY_QOS = new HashMap<>();

    static  {
        SEND_BY_QOS.put(QoS.AT_MOST_ONCE_DELIVERY, SimplePublishingService::sendByQos0);
        SEND_BY_QOS.put(QoS.AT_LEAST_ONCE_DELIVERY, SimplePublishingService::sendByQos1);
        SEND_BY_QOS.put(QoS.EXACTLY_ONCE_DELIVERY, SimplePublishingService::sendByQos2);
    }

    private static boolean sendByQos0(
        @NotNull MqttClient mqttClient,
        @NotNull PublishInPacket publish
    ) {
        mqttClient.send(mqttClient.getPacketOutFactory().newPublish(
            mqttClient,
            publish.getPacketId(),
            publish.getQos(),
            publish.isRetained(),
            publish.isDuplicate(),
            publish.getTopicName().toString(),
            publish.getTopicAlias(),
            publish.getPayload(),
            publish.isPayloadFormatIndicator(),
            publish.getResponseTopic(),
            publish.getCorrelationData(),
            publish.getUserProperties()
        ));
        return true;
    }

    private static boolean sendByQos1(
        @NotNull MqttClient mqttClient,
        @NotNull PublishInPacket publish
    ) {
        throw new UnsupportedOperationException("Send by QoS 1 is not supported");
    }

    private static boolean sendByQos2(
        @NotNull MqttClient mqttClient,
        @NotNull PublishInPacket publish
    ) {
        throw new UnsupportedOperationException("Send by QoS 2 is not supported");
    }

    private static boolean publishPacket(
        @NotNull Subscriber subscriber,
        @NotNull PublishInPacket packet
    ) {
        return SEND_BY_QOS.get(subscriber.getQos()).apply(subscriber.getMqttClient(), packet);
    }

    private final @NotNull SubscriptionService subscriptionService;

    @Override
    public @NotNull PublishAckReasonCode publish(@NotNull PublishInPacket publishPacket) {

        var result = subscriptionService.forEachTopicSubscriber(
            publishPacket.getTopicName(),
            publishPacket,
            SimplePublishingService::publishPacket
        );

        switch (result) {
            case EMPTY:
                return PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS;
            case SUCCESS:
                return PublishAckReasonCode.SUCCESS;
            case FAILED:
            default:
                return PublishAckReasonCode.UNSPECIFIED_ERROR;
        }
    }
}
