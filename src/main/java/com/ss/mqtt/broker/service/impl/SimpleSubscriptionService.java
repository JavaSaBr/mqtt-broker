package com.ss.mqtt.broker.service.impl;

import static com.ss.mqtt.broker.model.ActionResult.EMPTY;
import static com.ss.mqtt.broker.model.ActionResult.FAILED;
import static com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode.*;
import static com.ss.mqtt.broker.util.TopicUtils.*;
import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.reason.code.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.model.topic.TopicName;
import com.ss.mqtt.broker.model.topic.TopicSubscribers;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.rlib.common.function.NotNullBiFunction;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple subscription service
 */
public class SimpleSubscriptionService implements SubscriptionService {

    private final @NotNull TopicSubscribers topicSubscribers = new TopicSubscribers();

    @Override
    public <A> @NotNull ActionResult forEachTopicSubscriber(
        @NotNull TopicName topicName,
        @NotNull A argument,
        @NotNull NotNullBiFunction<SingleSubscriber, A, ActionResult> action
    ) {
        if (isInvalid(topicName)) {
            return FAILED;
        }
        ActionResult result = EMPTY;
        for (var subscriber : topicSubscribers.matches(topicName)) {
            result = result.and(action.apply(subscriber, argument));
        }
        return result;
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
        @NotNull MqttClient client
    ) {
        var config = client.getConnectionConfig();
        var topic = subscribe.getTopicFilter();
        var session = client.getSession();
        if (session == null) {
            return null;
        } else if (!config.isSharedSubscriptionAvailable() && isShared(topic)) {
            return SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
        } else if (!config.isWildcardSubscriptionAvailable() && hasWildcard(topic)) {
            return SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;
        } else if (isInvalid(topic)) {
            return SubscribeAckReasonCode.UNSPECIFIED_ERROR;
        } else {
            session.addSubscriber(subscribe);
            topicSubscribers.addSubscriber(client, subscribe);
            return subscribe.getQos().getSubscribeAckReasonCode();
        }
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
        @NotNull TopicFilter topic,
        @NotNull MqttClient client
    ) {
        var session = client.getSession();
        if (session == null) {
            return null;
        } else if (isInvalid(topic)) {
            return UNSPECIFIED_ERROR;
        } else if (topicSubscribers.removeSubscriber(client, topic)) {
            session.removeSubscriber(topic);
            return SUCCESS;
        } else {
            return NO_SUBSCRIPTION_EXISTED;
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
