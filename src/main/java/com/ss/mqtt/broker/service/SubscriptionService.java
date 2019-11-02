package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

/**
 * Subscription service
 */
public interface SubscriptionService {

    /**
     * Adds MQTT client to topic filter subscribers
     *
     * @param mqttClient MQTT client to be added
     * @param topicFilters topic filters
     * @return array of subscribe ack reason codes
     */
    @NotNull Array<SubscribeAckReasonCode> subscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<SubscribeTopicFilter> topicFilters
    );

    /**
     * Removes MQTT client from subscribers by array of topic names
     *
     * @param mqttClient MQTT client to be removed
     * @param topicNames topic names
     * @return array of unsubscribe ack reason codes
     */
    @NotNull Array<UnsubscribeAckReasonCode> unsubscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<String> topicNames
    );

    /**
     * Returns subscribers by topic name
     *
     * @param topicName topic name
     * @return array of topic subscribers
     */
    @NotNull Array<MqttClient> getSubscribers(@NotNull String topicName);
}
