package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.exception.InconsistentSubscriptionStateException;
import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.rlib.common.function.NotNullNullableTripleFunction;
import com.ss.rlib.common.function.NotNullSupplier;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import com.ss.rlib.common.util.array.ConcurrentArray;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Simple subscription service
 */
@RequiredArgsConstructor
public class SimpleSubscriptionService implements SubscriptionService {

    private final static NotNullSupplier<ConcurrentArray<Subscriber>> SUBSCRIBER_ARRAY_SUPPLIER =
        ConcurrentArray.supplier(Subscriber.class);

    private final static NotNullSupplier<ConcurrentArray<SubscribeTopicFilter>> TOPIC_ARRAY_SUPPLIER =
        ConcurrentArray.supplier(SubscribeTopicFilter.class);

    private final ConcurrentObjectDictionary<String, ConcurrentArray<Subscriber>> topicSubscribers =
        ConcurrentObjectDictionary.ofType(String.class, ConcurrentArray.class);

    private final ConcurrentObjectDictionary<MqttClient, ConcurrentArray<SubscribeTopicFilter>> clientSubscriptions =
        ConcurrentObjectDictionary.ofType(MqttClient.class, ConcurrentArray.class);

    private <A> @NotNull ActionResult applyToSubscribers(
        @NotNull ConcurrentArray<Subscriber> subscribers,
        @NotNull NotNullNullableTripleFunction<ConcurrentArray<SubscribeTopicFilter>, MqttClient, A, Boolean> action,
        @NotNull A argument
    ) {
        var actionResult = ActionResult.SUCCESS;
        for (var subscriber : subscribers) {
            var subscriptions = clientSubscriptions.getInReadLock(
                subscriber,
                (clSubs, sub) -> clSubs.get(sub.getMqttClient())
            );

            checkSubscriptionState(true, subscriptions != null);
            //noinspection ConstantConditions
            boolean result = subscriptions.getInReadLock(subscriber.getMqttClient(), argument, action);
            if (!result) {
                actionResult = ActionResult.FAILED;
            }
        }
        return actionResult;
    }

    @Override
    public <A> @NotNull ActionResult forEachTopicSubscriber(
        @NotNull String topicName,
        @NotNull A argument,
        @NotNull NotNullNullableTripleFunction<ConcurrentArray<SubscribeTopicFilter>, MqttClient, A, Boolean> action
    ) {
        var subscribers = topicSubscribers.getInReadLock(topicName, ObjectDictionary::get);
        if (subscribers == null || subscribers.isEmpty()) {
            return ActionResult.EMPTY;
        }

        //noinspection ConstantConditions
        return subscribers.getInReadLock(action, argument, this::applyToSubscribers);
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
        @NotNull SubscribeTopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    ) {

        addTopicSubscriber(topicFilter, mqttClient);
        addClientSubscriprion(topicFilter, mqttClient);

        return topicFilter.getQos().getSubscribeAckReasonCode();
    }

    private void addTopicSubscriber(
        @NotNull SubscribeTopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    ) {
        var subscribers = topicSubscribers.getInReadLock(topicFilter.getTopicName(), ObjectDictionary::get);

        if (subscribers == null) {
            subscribers = topicSubscribers.getInWriteLock(
                topicFilter.getTopicName(),
                SUBSCRIBER_ARRAY_SUPPLIER,
                ObjectDictionary::getOrCompute
            );
        }

        var subscriber = new Subscriber(mqttClient, topicFilter);
        // TODO implement subscription replacing if exists with the same topic name
        //noinspection ConstantConditions
        subscribers.runInWriteLock(subscriber, Array::add);
    }

    private void addClientSubscriprion(
        @NotNull SubscribeTopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    ){
        var clientSubscribers = clientSubscriptions.getInReadLock(mqttClient, ObjectDictionary::get);
        if (clientSubscribers == null) {
            clientSubscribers = clientSubscriptions.getInWriteLock(
                mqttClient,
                TOPIC_ARRAY_SUPPLIER,
                ObjectDictionary::getOrCompute
            );
        }

        //noinspection ConstantConditions
        clientSubscribers.runInWriteLock(topicFilter, Array::add);
    }

    @Override
    public @NotNull Array<UnsubscribeAckReasonCode> unsubscribe(
        @NotNull MqttClient mqttClient,
        @NotNull Array<String> topicNames
    ) {
        return topicNames.stream()
            .map(subscribeTopicFilter -> removeSubscription(subscribeTopicFilter, mqttClient))
            .collect(ArrayCollectors.toArray(UnsubscribeAckReasonCode.class));
    }

    private @NotNull UnsubscribeAckReasonCode removeSubscription(
        @NotNull String topicName,
        @NotNull MqttClient mqttClient
    ) {
        var subscribers = topicSubscribers.getInReadLock(topicName, ObjectDictionary::get);
        var subscriptions = clientSubscriptions.getInReadLock(mqttClient, ObjectDictionary::get);

        if (checkSubscriptionState(subscribers != null, subscriptions != null)) {
            boolean removed = subscribers.removeIfInWriteLock(
                mqttClient,
                (client, subscriber) -> client.equals(subscriber.getMqttClient())
            );

            boolean subscriptionRemoved = subscriptions.removeIfInWriteLock(
                topicName,
                (topic, subscriber) -> topic.equals(subscriber.getTopicName())
            );

            if (checkSubscriptionState(removed, subscriptionRemoved)) {
                return UnsubscribeAckReasonCode.SUCCESS;
            }
        }
        return UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
    }

    private static boolean checkSubscriptionState(boolean subscriberState, boolean subscriptionState) {
        if (subscriberState && !subscriptionState) {
            throw new InconsistentSubscriptionStateException(
                "Client subscription is present but topic subscriber could not be found ");
        } else if (!subscriberState && subscriptionState) {
            throw new InconsistentSubscriptionStateException(
                "Topic subscriber is present but client subscription could not be found ");
        } else {
            return subscriberState;
        }
    }
}
