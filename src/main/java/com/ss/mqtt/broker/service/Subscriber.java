package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.SubscribeRetainHandling;
import com.ss.mqtt.broker.network.MqttClient;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@Builder
@RequiredArgsConstructor
public class Subscriber {

    private final MqttClient mqttClient;
    private final QoS qos;
    private final SubscribeRetainHandling retainHandling;
    private final boolean noLocal;
    private final boolean retainAsPublished;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscriber that = (Subscriber) o;
        return Objects.equals(mqttClient, that.mqttClient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mqttClient);
    }
}
