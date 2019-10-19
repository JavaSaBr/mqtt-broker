package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleSubscriptionService implements SubscriptionService {

    private Map<SubscribeTopicFilter, Array<MqttClient>> subscriptions = new HashMap<>();

    @Override
    public void subscribe(@NotNull MqttClient mqttClient, @NotNull Array<SubscribeTopicFilter> topicFilter) {

        topicFilter.forEach(subscribeTopicFilter -> subscriptions.merge(subscribeTopicFilter,
            ArrayFactory.asArray(mqttClient),
            (oldValue, newValue) -> {
                oldValue.addAll(newValue);
                return oldValue;
            }
        ));
    }

    @Override
    public void unsubscribe(@NotNull MqttClient mqttClient, @NotNull Array<SubscribeTopicFilter> topicFilter) {

    }

    @Override
    public @NotNull List<MqttClient> getSubscribers(@NotNull String topic) {

        return subscriptions.entrySet()
            .stream()
            .filter(entry -> topic.equals(entry.getKey().getTopicFilter()))
            .flatMap(stream -> stream.getValue().stream())
            .collect(Collectors.toList());
    }
}
