package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishAckInPacket;
import com.ss.mqtt.broker.network.packet.in.PublishCompleteInPacket;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class PublishCompleteInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, PublishCompleteInPacket> {

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull PublishCompleteInPacket packet) {
        var session = client.getSession();
        if (session != null) {
            session.updateOutPendingPacket(client, packet);
        }
    }
}
