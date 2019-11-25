package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishAckInPacket;
import com.ss.mqtt.broker.network.packet.in.PublishReleaseInPacket;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class PublishReleaseInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, PublishReleaseInPacket> {

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull PublishReleaseInPacket packet) {
        var session = client.getSession();
        if (session != null) {
            session.updateInPendingPacket(client, packet);
        }
    }
}
