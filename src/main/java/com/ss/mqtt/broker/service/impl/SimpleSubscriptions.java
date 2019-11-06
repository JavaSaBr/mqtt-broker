package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.service.Subscriptions;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import com.ss.rlib.common.util.array.impl.FastArraySet;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple container of subscriptions
 */
public class SimpleSubscriptions implements Subscriptions {

    private final @NotNull Map<String, Array<Subscriber>> subscriptions = new HashMap<>();

    public @NotNull Array<MqttClient> getSubscribers(@NotNull String topicName) {
        return subscriptions.get(topicName)
            .stream()
            .map(Subscriber::getMqttClient)
            .collect(ArrayCollectors.toArray(MqttClient.class));
    }

    public @NotNull SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    ) {
        var subscriber = new Subscriber(mqttClient, topicFilter);
        var subscribers = subscriptions.computeIfAbsent(
            topicFilter.getTopicFilter(),
            key -> new FastArraySet<>(Subscriber.class)
        );
        subscribers.add(subscriber);
        return topicFilter.getQos().getSubscribeAckReasonCode();
    }

    public @NotNull UnsubscribeAckReasonCode removeSubscription(
        @NotNull String topicName,
        @NotNull MqttClient mqttClient
    ) {
        var subscribers = subscriptions.getOrDefault(topicName, Array.empty());
        if (subscribers.removeIf(subscriber -> mqttClient.equals(subscriber.getMqttClient()))) {
            return UnsubscribeAckReasonCode.SUCCESS;
        } else {
            return UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
        }
    }
}
