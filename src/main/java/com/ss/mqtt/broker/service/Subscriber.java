package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.SubscribeRetainHandling;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.network.client.impl.DeviceMqttClient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public class Subscriber {

    private final DeviceMqttClient mqttClient;
    private final QoS qos;
    private final SubscribeRetainHandling retainHandling;
    private final boolean noLocal;
    private final boolean retainAsPublished;

    public Subscriber(@NotNull DeviceMqttClient mqttClient, @NotNull SubscribeTopicFilter topicFilter) {
        this.mqttClient = mqttClient;
        this.qos = topicFilter.getQos();
        this.retainHandling = topicFilter.getRetainHandling();
        this.retainAsPublished = topicFilter.isRetainAsPublished();
        this.noLocal = topicFilter.isNoLocal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (Subscriber) o;
        return Objects.equals(mqttClient, that.mqttClient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mqttClient);
    }
}
