package com.ss.mqtt.broker.network.packet.in.handler;

import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.UnsubscribeInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class UnsubscribeInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, UnsubscribeInPacket> {

    private final @NotNull SubscriptionService subscriptionService;

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull UnsubscribeInPacket packet) {

        var ackReasonCodes = subscriptionService.unsubscribe(client, packet.getTopicFilters());

        client.send(client.getPacketOutFactory().newUnsubscribeAck(client,
            packet.getPacketId(),
            ackReasonCodes
        ));
    }
}
