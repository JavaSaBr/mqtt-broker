package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.function.NotNullNullableTripleFunction;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ConcurrentArray;
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
        @NotNull String topicName,
        @NotNull A argument,
        @NotNull NotNullNullableTripleFunction<ConcurrentArray<SubscribeTopicFilter>, MqttClient, A, Boolean> action
    );

    /**
     * Adds MQTT client to topic filter subscribers
     *
     * @param mqttClient MQTT client to be added
     * @param topicNames topic names
     * @return array of subscribe ack reason codes
     */
    @NotNull Array<SubscribeAckReasonCode> subscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<SubscribeTopicFilter> topicNames
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
}
