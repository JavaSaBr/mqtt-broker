package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.network.client.MqttClient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode(of = "mqttClient")
@RequiredArgsConstructor
public class SingleSubscriber implements Subscriber {

    private final @Getter @NotNull MqttClient mqttClient;
    private final @NotNull SubscribeTopicFilter subscribe;

    public @NotNull QoS getQos() {
        return subscribe.getQos();
    }
}
