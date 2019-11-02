package com.ss.mqtt.broker.network.packet.in.handler;

import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import org.jetbrains.annotations.NotNull;

public interface PacketInHandler {

    @NotNull PacketInHandler EMPTY = (client, packet) -> {};

    void handle(@NotNull UnsafeMqttClient client, @NotNull MqttReadablePacket packet);
}
