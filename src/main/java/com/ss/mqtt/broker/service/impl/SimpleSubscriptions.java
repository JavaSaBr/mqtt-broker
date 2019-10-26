package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.service.Subscriber;
import com.ss.mqtt.broker.service.Subscriptions;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import com.ss.rlib.common.util.array.impl.FastArraySet;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SimpleSubscriptions implements Subscriptions {

    private final Map<String, Array<Subscriber>> subscriptions = new HashMap<>();

    /**
     * Return full subscribers list
     */
    public @NotNull Array<MqttClient> getSubscribers(@NotNull String topic) {
        return subscriptions.get(topic)
            .stream()
            .map(Subscriber::getMqttClient)
            .collect(ArrayCollectors.toArray(MqttClient.class));
    }

    /**
     * Return true if subscription is added
     */
    public @NotNull SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    ) {
        Subscriber subscriber = new Subscriber(mqttClient, topicFilter);
        var subscribers = subscriptions.computeIfAbsent(
            topicFilter.getTopicFilter(),
            key -> new FastArraySet<>(Subscriber.class)
        );
        subscribers.add(subscriber);
        return topicFilter.getQos().getSubscribeAckReasonCode();
    }

    /**
     * Return true if subscription is removed
     */
    public @NotNull UnsubscribeAckReasonCode removeSubscription(@NotNull String topicFilter, @NotNull MqttClient mqttClient) {
        var subscribers = subscriptions.getOrDefault(topicFilter, Array.empty());
        if (subscribers.removeIf(subscriber -> mqttClient.equals(subscriber.getMqttClient()))) {
            return UnsubscribeAckReasonCode.SUCCESS;
        }
        return UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
    }

}
