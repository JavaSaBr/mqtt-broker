package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.SubscribeInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class SubscribeInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, SubscribeInPacket> {

    private final @NotNull SubscriptionService subscriptionService;

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull SubscribeInPacket packet) {

        var ackReasonCodes = subscriptionService.subscribe(client, packet.getTopicFilters());

        client.send(client.getPacketOutFactory().newSubscribeAck(client,
            packet.getPacketId(),
            ackReasonCodes
        ));
    }
}
