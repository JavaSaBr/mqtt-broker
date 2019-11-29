package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.network.client.MqttClient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode(of = "mqttClient")
public class Subscriber {

    private final @Getter @NotNull MqttClient mqttClient;
    private final @NotNull SubscribeTopicFilter subscribeTopicFilter;

    /**
     * Creates subscriber
     *
     * @param mqttClient  MQTT client which will become a subscriber
     * @param topicFilter topic filter that MQTT client subscribes to
     */
    public Subscriber(@NotNull MqttClient mqttClient, @NotNull SubscribeTopicFilter topicFilter) {
        this.mqttClient = mqttClient;
        this.subscribeTopicFilter = topicFilter;
    }

    public @NotNull QoS getQos() {
        return subscribeTopicFilter.getQos();
    }

    public @NotNull TopicFilter getTopicFilter() {
        return subscribeTopicFilter.getTopicFilter();
    }

}
