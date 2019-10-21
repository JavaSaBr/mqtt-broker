package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Subscriptions {

    private final Map<String, Set<MqttClient>> allSubscriptions = new HashMap<>();

    private final Map<String, Set<MqttClient>> noLocalTrue = new HashMap<>();
    private final Map<String, Set<MqttClient>> noLocalFalse = new HashMap<>();

    private final Map<String, Set<MqttClient>> retainAsPublishedTrue = new HashMap<>();
    private final Map<String, Set<MqttClient>> retainAsPublishedFalse = new HashMap<>();

    private final Map<String, Set<MqttClient>> sendAtTheTimeOfSubscribe = new HashMap<>();
    private final Map<String, Set<MqttClient>> sendAtTheTimeOfSubscribeIfTheSubscriptionDoesNotCurrentlyExist = new HashMap<>();
    private final Map<String, Set<MqttClient>> doNotSendAtTheTimeOfSubscribe = new HashMap<>();

    private final Map<String, Set<MqttClient>> atMostOnceDelivery = new HashMap<>();
    private final Map<String, Set<MqttClient>> atLeastOnceDelivery = new HashMap<>();
    private final Map<String, Set<MqttClient>> exactlyOnceDelivery = new HashMap<>();

    /**
     * Return full subscribers list
     */
    public List<MqttClient> getSubscribers(@NotNull String topic) {
        return new ArrayList<>(allSubscriptions.get(topic));
    }

    /**
     * Return true if subscription is added
     */
    public boolean addSubscription(@NotNull SubscribeTopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        return executeProcessor(topicFilter, mqttClient, Subscriptions::addMqttClient);
    }

    /**
     * Return true if subscription is removed
     */
    public boolean removeSubscription(@NotNull SubscribeTopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        return executeProcessor(topicFilter, mqttClient, Subscriptions::removeMqttClient);
    }

    private boolean executeProcessor(
        @NotNull SubscribeTopicFilter topicFilter, @NotNull MqttClient mqttClient, @NotNull Processor processor
    ) {

        if (topicFilter.isNoLocal()) {
            processor.execute(topicFilter.getTopicFilter(), mqttClient, noLocalTrue);
        } else {
            processor.execute(topicFilter.getTopicFilter(), mqttClient, noLocalFalse);
        }

        if (topicFilter.isRetainAsPublished()) {
            processor.execute(topicFilter.getTopicFilter(), mqttClient, retainAsPublishedTrue);
        } else {
            processor.execute(topicFilter.getTopicFilter(), mqttClient, retainAsPublishedFalse);
        }

        switch (topicFilter.getQos()) {
            case AT_LEAST_ONCE_DELIVERY:
                processor.execute(topicFilter.getTopicFilter(), mqttClient, atLeastOnceDelivery);
                break;
            case AT_MOST_ONCE_DELIVERY:
                processor.execute(topicFilter.getTopicFilter(), mqttClient, atMostOnceDelivery);
                break;
            case EXACTLY_ONCE_DELIVERY:
                processor.execute(topicFilter.getTopicFilter(), mqttClient, exactlyOnceDelivery);
                break;
        }

        switch (topicFilter.getRetainHandling()) {
            case DO_NOT_SEND_AT_THE_TIME_OF_THE_SUBSCRIBE:
                processor.execute(topicFilter.getTopicFilter(), mqttClient, doNotSendAtTheTimeOfSubscribe);
                break;
            case SEND_AT_SUBSCRIBE_ONLY_IF_THE_SUBSCRIPTION_DOES_NOT_CURRENTLY_EXIST:
                processor.execute(
                    topicFilter.getTopicFilter(),
                    mqttClient,
                    sendAtTheTimeOfSubscribeIfTheSubscriptionDoesNotCurrentlyExist
                );
                break;
            case SEND_AT_THE_TIME_OF_SUBSCRIBE:
                processor.execute(topicFilter.getTopicFilter(), mqttClient, sendAtTheTimeOfSubscribe);
                break;
        }

        return processor.execute(topicFilter.getTopicFilter(), mqttClient, allSubscriptions);
    }

    private static boolean removeMqttClient(
        @NotNull String topicFilter, @NotNull MqttClient mqttClient, @NotNull Map<String, Set<MqttClient>> subscriptions
    ) {
        Set<MqttClient> clients = subscriptions.get(topicFilter);
        return clients.remove(mqttClient);
    }

    private static boolean addMqttClient(
        @NotNull String topicFilter, @NotNull MqttClient mqttClient, @NotNull Map<String, Set<MqttClient>> subscriptions
    ) {
        Set<MqttClient> clients = subscriptions.get(topicFilter);
        if (clients == null) {
            clients = new HashSet<>();
            clients.add(mqttClient);
            subscriptions.put(topicFilter, clients);
            return true;
        } else {
            return clients.add(mqttClient);
        }
    }

    interface Processor {

        boolean execute(
            @NotNull String topicFilter,
            @NotNull MqttClient mqttClient,
            @NotNull Map<String, Set<MqttClient>> subscriptions
        );
    }
}
