package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPacketHandler<C extends UnsafeMqttClient, R extends MqttReadablePacket> implements
    PacketInHandler {

    @Override
    public void handle(@NotNull UnsafeMqttClient client, @NotNull MqttReadablePacket packet) {
        handleImpl((C) client, (R) packet);
    }

    protected abstract void handleImpl(@NotNull C client, @NotNull R packet);
}
