package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishReleaseInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishReleaseInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, PublishReleaseInPacket> {

    @Override
    protected void handleImpl(UnsafeMqttClient client, PublishReleaseInPacket packet) {
        var session = client.getSession();
        if (session != null) {
            session.updateInPendingPacket(client, packet);
        }
    }
}
