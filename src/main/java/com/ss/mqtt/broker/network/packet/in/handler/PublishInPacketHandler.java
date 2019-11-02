package com.ss.mqtt.broker.network.packet.in.handler;

import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.PublishingService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class PublishInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, PublishInPacket> {

    private final @NotNull PublishingService publishingService;

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull PublishInPacket packet) {

        var ackReasonCode = publishingService.publish(packet);

        client.send(client.getPacketOutFactory().newPublishAck(
            client,
            packet.getPacketId(),
            ackReasonCode
        ));
    }
}
