package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.service.Subscriber;
import com.ss.mqtt.broker.service.Subscriptions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleSubscriptions implements Subscriptions {

    private final Map<String, Set<Subscriber>> subscriptions = new HashMap<>();

    /**
     * Return full subscribers list
     */
    public List<MqttClient> getSubscribers(@NotNull String topic) {
        return subscriptions.get(topic).stream().map(Subscriber::getMqttClient).collect(Collectors.toList());
    }

    /**
     * Return true if subscription is added
     */
    public SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter topicFilter, @NotNull MqttClient mqttClient
    ) {
        Subscriber subscriber = Subscriber.builder()
            .mqttClient(mqttClient)
            .noLocal(topicFilter.isNoLocal())
            .qos(topicFilter.getQos())
            .retainAsPublished(topicFilter.isRetainAsPublished())
            .retainHandling(topicFilter.getRetainHandling())
            .build();
        Set<Subscriber> subscribers = subscriptions.computeIfAbsent(topicFilter.getTopicFilter(),
            key -> new HashSet<>()
        );
        subscribers.add(subscriber);
        return topicFilter.getQos().getSubscribeAckReasonCode();
    }

    /**
     * Return true if subscription is removed
     */
    public UnsubscribeAckReasonCode removeSubscription(
        @NotNull String topicFilter, @NotNull MqttClient mqttClient
    ) {
        Set<Subscriber> subscribers = subscriptions.get(topicFilter);
        if (subscribers == null) {
            return UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
        }
        long existed = subscribers.stream()
            .filter(subscriber -> mqttClient.getServerClientId().equals(subscriber.getMqttClient().getServerClientId()))
            .map(subscribers::remove)
            .count();
        if (existed == 0) {
            return UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
        }
        return UnsubscribeAckReasonCode.SUCCESS;
    }

}
