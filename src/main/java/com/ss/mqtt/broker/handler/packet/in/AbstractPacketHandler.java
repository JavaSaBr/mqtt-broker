package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.exception.SubscriptionRejectException;
import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPacketHandler<C extends UnsafeMqttClient, R extends MqttReadablePacket> implements
    PacketInHandler {

    @Override
    public void handle(@NotNull UnsafeMqttClient client, @NotNull MqttReadablePacket packet) {
        try {
            //noinspection unchecked
            handleImpl((C) client, (R) packet);
        } catch (SubscriptionRejectException reject) {
            client.send(client.getPacketOutFactory().newDisconnect(client, reject.getReasonCode()));
            client.getConnection().close();
        }
    }

    protected abstract void handleImpl(@NotNull C client, @NotNull R packet);
}

