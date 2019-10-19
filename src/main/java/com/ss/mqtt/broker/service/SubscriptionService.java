package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SubscriptionService {

    void subscribe(@NotNull MqttClient mqttClient, @NotNull Array<SubscribeTopicFilter> topicFilter);

    void unsubscribe(@NotNull MqttClient mqttClient, @NotNull Array<SubscribeTopicFilter> topicFilter);

    List<MqttClient> getSubscribers(@NotNull String topic);
}
