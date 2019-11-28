package com.ss.mqtt.broker.service.impl;

import static com.ss.mqtt.broker.model.ActionResult.*;
import com.ss.mqtt.broker.model.ActionResult;
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
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Simple subscription service
 */
@RequiredArgsConstructor
public class SimpleSubscriptionService implements SubscriptionService {

    private final TopicSubscribers topicSubscribers;

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

    private @NotNull SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter subscribe,
        @NotNull MqttClient mqttClient
    ) {
        var session = mqttClient.getSession();
        if (session != null) {
            session.getTopicFilters().runInWriteLock(subscribe, Collection::add);
        }
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

    private @NotNull UnsubscribeAckReasonCode removeSubscription(
        @NotNull TopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    ) {
        var session = mqttClient.getSession();
        if (session != null) {
            session.getTopicFilters()
                .removeIfConvertedInWriteLock(
                    topicFilter,
                    SubscribeTopicFilter::getTopicFilter,
                    Object::equals
                );
        }
        if (topicSubscribers.removeSubscriber(mqttClient, topicFilter)) {
            return UnsubscribeAckReasonCode.SUCCESS;
        } else {
            return UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
        }
    }

}
