package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import org.jetbrains.annotations.NotNull;

public interface PublishingService {

    void publish(@NotNull MqttClient client, @NotNull PublishInPacket publish);
}
