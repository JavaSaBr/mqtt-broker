package com.ss.mqtt.broker.service.impl;

import static com.ss.mqtt.broker.model.ActionResult.*;
import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.model.topic.TopicName;
import com.ss.mqtt.broker.model.topic.TopicSubscribers;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.rlib.common.function.NotNullNullableBiFunction;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple subscription service
 */
public class SimpleSubscriptionService implements SubscriptionService {

    private final TopicSubscribers topicSubscribers = new TopicSubscribers();

    @Override
    public <A> @NotNull ActionResult forEachTopicSubscriber(
        @NotNull TopicName topicName,
        @NotNull A argument,
        @NotNull NotNullNullableBiFunction<Subscriber, A, Boolean> action
    ) {
        var subscribers = topicSubscribers.matches(topicName);
        if (subscribers.isEmpty()) {
            return EMPTY;
        }
        boolean result = true;
        for (var subscriber : subscribers) {
            //noinspection ConstantConditions
            result = result && action.apply(subscriber, argument);
        }
        return result ? SUCCESS : FAILED;
    }

    @Override
    public @NotNull Array<SubscribeAckReasonCode> subscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<SubscribeTopicFilter> topicFilters
    ) {
        return topicFilters.stream()
            .map(topicFilter -> addSubscription(topicFilter, mqttClient))
            .collect(ArrayCollectors.toArray(SubscribeAckReasonCode.class));
    }

    private @Nullable SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter subscribe,
        @NotNull MqttClient mqttClient
    ) {
        var session = mqttClient.getSession();
        if (session == null) {
            return null;
        }
        session.addSubscriber(subscribe);
        topicSubscribers.addSubscriber(mqttClient, subscribe);
        return subscribe.getQos().getSubscribeAckReasonCode();
    }

    @Override
    public @NotNull Array<UnsubscribeAckReasonCode> unsubscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<TopicFilter> topicFilters
    ) {
        return topicFilters.stream()
            .map(topicFilter -> removeSubscription(topicFilter, mqttClient))
            .collect(ArrayCollectors.toArray(UnsubscribeAckReasonCode.class));
    }

    private @Nullable UnsubscribeAckReasonCode removeSubscription(
        @NotNull TopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    ) {
        var session = mqttClient.getSession();
        if (session == null) {
            return null;
        }
        if (topicSubscribers.removeSubscriber(mqttClient, topicFilter)) {
            session.removeSubscriber(topicFilter);
            return UnsubscribeAckReasonCode.SUCCESS;
        } else {
            return UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
        }
    }

    public void cleanSubscriptions(@NotNull MqttClient mqttClient, @NotNull MqttSession mqttSession){
        mqttSession.forEachTopicFilter(
            topicSubscribers,
            mqttClient,
            TopicSubscribers::removeSubscriber
        );
    }

    public void restoreSubscriptions(@NotNull MqttClient mqttClient, @NotNull MqttSession mqttSession){
        mqttSession.forEachTopicFilter(
            topicSubscribers,
            mqttClient,
            TopicSubscribers::addSubscriber
        );
    }
}
