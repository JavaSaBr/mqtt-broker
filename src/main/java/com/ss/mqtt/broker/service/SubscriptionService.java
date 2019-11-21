package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.model.topic.TopicName;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.function.NotNullNullableBiFunction;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

/**
 * Subscription service
 */
public interface SubscriptionService {

    /**
     * Runs function for each topic subscriber
     *
     * @param topicName topic name
     * @param argument additional argument
     * @param action function to run
     * @return {@link ActionResult} of function
     */
    @NotNull <A> ActionResult forEachTopicSubscriber(
        @NotNull TopicName topicName,
        @NotNull A argument,
        @NotNull NotNullNullableBiFunction<Subscriber, A, Boolean> action
    );

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
     * @param topicFilters topic filters
     * @return array of unsubscribe ack reason codes
     */
    @NotNull Array<UnsubscribeAckReasonCode> unsubscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<TopicFilter> topicFilters
    );
}
