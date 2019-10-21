package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.mqtt.broker.service.Subscriptions;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SimpleSubscriptionService implements SubscriptionService {

    private Subscriptions subscriptions = new Subscriptions();

    @Override
    public void subscribe(@NotNull MqttClient mqttClient, @NotNull Array<SubscribeTopicFilter> topicFilter) {

        topicFilter.forEach(subscribeTopicFilter -> subscriptions.addSubscription(subscribeTopicFilter, mqttClient));
    }

    @Override
    public void unsubscribe(@NotNull MqttClient mqttClient, @NotNull Array<SubscribeTopicFilter> topicFilter) {

        topicFilter.forEach(subscribeTopicFilter -> subscriptions.removeSubscription(subscribeTopicFilter, mqttClient));
    }

    @Override
    public @NotNull List<MqttClient> getSubscribers(@NotNull String topic) {

        return subscriptions.getSubscribers(topic);
    }
}
