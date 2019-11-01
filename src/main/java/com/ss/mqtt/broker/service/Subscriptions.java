package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.impl.DeviceMqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

public interface Subscriptions {

    /**
     * Return full subscribers list
     */
    @NotNull Array<DeviceMqttClient> getSubscribers(@NotNull String topic);

    /**
     * Return true if subscription is added
     */
    @NotNull SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter topicFilter,
        @NotNull DeviceMqttClient mqttClient
    );

    /**
     * Return true if subscription is removed
     */
    @NotNull UnsubscribeAckReasonCode removeSubscription(
        @NotNull String topicFilter,
        @NotNull DeviceMqttClient mqttClient
    );
}
