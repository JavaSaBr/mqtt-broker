package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.service.SubscriptionService;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SimpleSubscriptionService implements SubscriptionService {

    private Map<SubscribeTopicFilter, MqttClient> subscriptions = new HashMap<>();

    @Override
    public void subscribe(@NotNull MqttClient mqttClient, @NotNull SubscribeTopicFilter topicFilter) {

        subscriptions.put(topicFilter, mqttClient);
    }

    @Override
    public void unsubscribe(@NotNull MqttClient mqttClient, @NotNull SubscribeTopicFilter topicFilter) {

    }

    @Override
    public void getSubscribers(@NotNull String topic) {

    }
}
