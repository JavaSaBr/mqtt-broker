package com.ss.mqtt.broker.service.impl;

import static com.ss.mqtt.broker.model.ActionResult.FAILED;
import static com.ss.mqtt.broker.model.ActionResult.SUCCESS;
import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.rlib.common.function.NotNullNullableBiFunction;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Simple subscription service
 */
@RequiredArgsConstructor
public class SimpleSubscriptionService implements SubscriptionService {

    private final TopicSubscriber topicSubscribers = new TopicSubscriber();

    @Override
    public <A> @NotNull ActionResult forEachTopicSubscriber(
        @NotNull TopicName topicName,
        @NotNull A argument,
        @NotNull NotNullNullableBiFunction<Subscriber, A, Boolean> action
    ) {

        boolean result = true;
        for (var subscriber : topicSubscribers.matches(topicName))  {
            //noinspection ConstantConditions
            result = result && action.apply(subscriber, argument);
        }

        return result ? SUCCESS : FAILED;
    }

    @Override
    public @NotNull Array<SubscribeAckReasonCode> subscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<SubscribeTopicFilter> topicNames
    ) {
        return topicNames.stream()
            .map(subscribeTopicFilter -> addSubscription(subscribeTopicFilter, mqttClient))
            .collect(ArrayCollectors.toArray(SubscribeAckReasonCode.class));
    }

    private @NotNull SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter subscribe,
        @NotNull MqttClient mqttClient
    ) {
        topicSubscribers.addSubscriber(subscribe.getTopicFilter(), new Subscriber(mqttClient, subscribe));
        return subscribe.getQos().getSubscribeAckReasonCode();
    }

    @Override
    public @NotNull Array<UnsubscribeAckReasonCode> unsubscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<TopicFilter> topicFilters
    ) {
        return topicFilters.stream()
            .map(subscribeTopicFilter -> removeSubscription(subscribeTopicFilter, mqttClient))
            .collect(ArrayCollectors.toArray(UnsubscribeAckReasonCode.class));
    }

    private @NotNull UnsubscribeAckReasonCode removeSubscription(
        @NotNull TopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    ) {
        if (topicSubscribers.removeSubscriber(topicFilter, mqttClient)) {
            return UnsubscribeAckReasonCode.SUCCESS;
        } else {
            return UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
        }
    }

}
