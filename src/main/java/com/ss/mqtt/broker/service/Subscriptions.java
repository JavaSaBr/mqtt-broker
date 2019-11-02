package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

public interface Subscriptions {

    /**
     * Return full subscribers list
     */
    @NotNull Array<MqttClient> getSubscribers(@NotNull String topic);

    /**
     * Return true if subscription is added
     */
    @NotNull SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    );

    /**
     * Return true if subscription is removed
     */
    @NotNull UnsubscribeAckReasonCode removeSubscription(
        @NotNull String topicFilter,
        @NotNull MqttClient mqttClient
    );
}
