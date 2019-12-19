package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.network.client.MqttClient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode(of = "mqttClient", callSuper = false)
public class SingleSubscriber extends AbstractSubscriber {

    private final @Getter @NotNull MqttClient mqttClient;

    public SingleSubscriber(@NotNull MqttClient mqttClient, @NotNull SubscribeTopicFilter topic) {
        super(topic);
        this.mqttClient = mqttClient;
    }
}
