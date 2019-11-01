package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.impl.DeviceMqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

public interface SubscriptionService {

    @NotNull Array<SubscribeAckReasonCode> subscribe(
        @NotNull DeviceMqttClient mqttClient,
        @NotNull Array<SubscribeTopicFilter> topicFilter
    );

    @NotNull Array<UnsubscribeAckReasonCode> unsubscribe(
        @NotNull DeviceMqttClient mqttClient,
        @NotNull Array<String> topicFilter
    );

    @NotNull Array<DeviceMqttClient> getSubscribers(@NotNull String topic);
}
