package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

public interface SubscriptionService {

    void subscribe(@NotNull MqttClient mqttClient, @NotNull SubscribeTopicFilter topicFilter);

    void unsubscribe(@NotNull MqttClient mqttClient, @NotNull SubscribeTopicFilter topicFilter);

    void getSubscribers(@NotNull String topic);
}
