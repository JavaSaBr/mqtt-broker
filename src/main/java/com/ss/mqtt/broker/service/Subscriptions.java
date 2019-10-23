package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Subscriptions {

    /**
     * Return full subscribers list
     */
    List<MqttClient> getSubscribers(@NotNull String topic);

    /**
     * Return true if subscription is added
     */
    SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter topicFilter, @NotNull MqttClient mqttClient
    );

    /**
     * Return true if subscription is removed
     */
    UnsubscribeAckReasonCode removeSubscription(
        @NotNull String topicFilter, @NotNull MqttClient mqttClient
    );
}
