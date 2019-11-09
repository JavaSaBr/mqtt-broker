package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.service.Subscriptions;
import com.ss.rlib.common.function.NotNullSupplier;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ConcurrentArray;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;

/**
 * Simple container of subscriptions
 */
public class SimpleSubscriptions implements Subscriptions {

    private final static @NotNull NotNullSupplier<ConcurrentArray<Subscriber>> SUBSCRIBER_ARRAY_SUPPLIER =
        ConcurrentArray.supplier(Subscriber.class);

    private final @NotNull ConcurrentObjectDictionary<String, ConcurrentArray<Subscriber>> subscriptions =
        ConcurrentObjectDictionary.ofType(String.class, ConcurrentArray.class);

    public @NotNull Array<Subscriber> getSubscribers(@NotNull String topicName) {

        var subscribers = subscriptions.getInReadLock(topicName, ObjectDictionary::get);
        if (subscribers == null) {
            return Array.empty();
        }

        //noinspection ConstantConditions
        return subscribers.getInReadLock(Array::of);
    }

    public @NotNull SubscribeAckReasonCode addSubscription(
        @NotNull SubscribeTopicFilter topicFilter,
        @NotNull MqttClient mqttClient
    ) {
        var subscriber = new Subscriber(mqttClient, topicFilter);
        var subscribers = subscriptions.getInReadLock(topicFilter.getTopicName(), ObjectDictionary::get);

        if (subscribers == null) {
            subscribers = subscriptions.getInWriteLock(
                topicFilter.getTopicName(),
                SUBSCRIBER_ARRAY_SUPPLIER,
                ObjectDictionary::getOrCompute
            );
        }

        //noinspection ConstantConditions
        subscribers.runInWriteLock(subscriber, Array::add);

        return topicFilter.getQos().getSubscribeAckReasonCode();
    }

    public @NotNull UnsubscribeAckReasonCode removeSubscription(
        @NotNull String topicName,
        @NotNull MqttClient mqttClient
    ) {
        var subscribers = subscriptions.getInReadLock(topicName, ObjectDictionary::get);

        if (subscribers == null) {
            return UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
        } else {
            boolean removed = subscribers.removeIfInWriteLock(
                mqttClient,
                (client, subscriber) -> client.equals(subscriber.getMqttClient())
            );

            return removed ? UnsubscribeAckReasonCode.SUCCESS : UnsubscribeAckReasonCode.NO_SUBSCRIPTION_EXISTED;
        }
    }
}
