package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.PublishingService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, PublishInPacket> {

    private final PublishingService publishingService;

    @Override
    protected void handleImpl(UnsafeMqttClient client, PublishInPacket packet) {
        publishingService.publish(client, packet);
    }
}
