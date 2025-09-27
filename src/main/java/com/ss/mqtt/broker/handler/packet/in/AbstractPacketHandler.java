package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;

public abstract class AbstractPacketHandler<C extends UnsafeMqttClient, R extends MqttReadablePacket> implements
    PacketInHandler {

    @Override
    public void handle(UnsafeMqttClient client, MqttReadablePacket packet) {
        //noinspection unchecked
        handleImpl((C) client, (R) packet);
    }

    protected abstract void handleImpl(C client, R packet);
}

