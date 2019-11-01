package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

/**
 * Container of subscriptions
 */
public interface Subscriptions {

    /**
     * Returns array of subscribers by topic name
     *
     * @param topicName topic name on which subscribers should be returned
     * @return array of MQTT clients
     */
    @NotNull Array<MqttClient> getSubscribers(@NotNull String topicName);

    /**
     * Returns result of subscription adding
     *
     * @param topicFilter topic filter that MQTT client wants to subscribe to
     * @param mqttClient MQTT client which wants to subscribe
     * @return subscribe ack reason code
     */
    @NotNull SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    );

    /**
     * Returns result of subscription removing
     *
     * @param topicName topic name that MQTT client wants to unsubscribe from
     * @param mqttClient MQTT client which wants to unsubscribe
     * @return unsubscribe ack reason code
     */
    @NotNull UnsubscribeAckReasonCode removeSubscription(
        @NotNull String topicName,
        @NotNull MqttClient mqttClient
    );
}
