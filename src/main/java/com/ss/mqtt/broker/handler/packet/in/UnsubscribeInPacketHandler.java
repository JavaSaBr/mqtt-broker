package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.UnsubscribeInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UnsubscribeInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, UnsubscribeInPacket> {

    private final SubscriptionService subscriptionService;

    @Override
    protected void handleImpl(UnsafeMqttClient client, UnsubscribeInPacket packet) {
        var ackReasonCodes = subscriptionService.unsubscribe(client, packet.getTopicFilters());
        client.send(client.getPacketOutFactory().newUnsubscribeAck(packet.getPacketId(), ackReasonCodes));
    }
}
