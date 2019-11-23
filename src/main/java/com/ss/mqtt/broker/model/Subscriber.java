package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.network.client.MqttClient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode(of = "mqttClient")
public class Subscriber {

    private final @NotNull MqttClient mqttClient;
    private final @NotNull QoS qos;
    private final @NotNull SubscribeRetainHandling retainHandling;

    private final boolean noLocal;
    private final boolean retainAsPublished;

    /**
     * Creates subscriber
     *
     * @param mqttClient  MQTT client which will become a subscriber
     * @param topicFilter topic filter that MQTT client subscribes to
     */
    public Subscriber(@NotNull MqttClient mqttClient, @NotNull SubscribeTopicFilter topicFilter) {
        this.mqttClient = mqttClient;
        this.qos = topicFilter.getQos();
        this.retainHandling = topicFilter.getRetainHandling();
        this.retainAsPublished = topicFilter.isRetainAsPublished();
        this.noLocal = topicFilter.isNoLocal();
    }
}
