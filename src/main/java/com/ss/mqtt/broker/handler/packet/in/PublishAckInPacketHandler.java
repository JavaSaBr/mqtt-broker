package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishAckInPacket;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class PublishAckInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, PublishAckInPacket> {

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull PublishAckInPacket packet) {
        client.getSession().unregisterPendingPacket(client, packet);
    }
}
