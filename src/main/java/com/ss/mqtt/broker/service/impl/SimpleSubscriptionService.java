package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.mqtt.broker.service.Subscriptions;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import com.ss.rlib.common.util.array.impl.FastArray;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
public class SimpleSubscriptionService implements SubscriptionService {

    private final Subscriptions subscriptions;

    @Override
    public Array<SubscribeAckReasonCode> subscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<SubscribeTopicFilter> topicFilter
    ) {
        return topicFilter.stream()
            .map(subscribeTopicFilter -> subscriptions.addSubscription(subscribeTopicFilter, mqttClient))
            .collect(ArrayCollectors.toArray(SubscribeAckReasonCode.class));
    }

    @Override
    public Array<UnsubscribeAckReasonCode> unsubscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<String> topicFilter
    ) {
        return topicFilter.stream()
            .map(subscribeTopicFilter -> subscriptions.removeSubscription(subscribeTopicFilter, mqttClient))
            .collect(ArrayCollectors.toArray(UnsubscribeAckReasonCode.class));
    }

    @Override
    public @NotNull Array<MqttClient> getSubscribers(@NotNull String topic) {
        return subscriptions.getSubscribers(topic);
    }
}
