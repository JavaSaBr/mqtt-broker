package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.network.client.MqttClient;
import org.jetbrains.annotations.NotNull;

public interface Subscriber {

    @NotNull MqttClient getMqttClient();

    @NotNull QoS getQos();

    @NotNull TopicFilter getTopicFilter();

}
