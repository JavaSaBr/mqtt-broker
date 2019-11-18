package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import org.jetbrains.annotations.NotNull;

public interface PacketInHandler {

    @NotNull PacketInHandler EMPTY = (client, packet) -> {};

    void handle(@NotNull MqttClient.UnsafeMqttClient client, @NotNull MqttReadablePacket packet);
}
