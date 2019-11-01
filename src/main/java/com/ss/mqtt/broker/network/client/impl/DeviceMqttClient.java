package com.ss.mqtt.broker.network.client.impl;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.in.SubscribeInPacket;
import com.ss.mqtt.broker.network.packet.in.UnsubscribeInPacket;
import org.jetbrains.annotations.NotNull;

public class DeviceMqttClient extends AbstractMqttClient {

    public DeviceMqttClient(@NotNull MqttConnection connection) {
        super(connection);
    }

    private void onSubscribe(@NotNull SubscribeInPacket subscribe) {

        var ackReasonCodes = connection.getSubscriptionService()
            .subscribe(this, subscribe.getTopicFilters());

        connection.send(getPacketOutFactory().newSubscribeAck(this,
            subscribe.getPacketId(),
            ackReasonCodes
        ));
    }

    private void onUnsubscribe(@NotNull UnsubscribeInPacket subscribe) {

        var ackReasonCodes = connection.getSubscriptionService()
            .unsubscribe(this, subscribe.getTopicFilters());

        connection.send(getPacketOutFactory().newUnsubscribeAck(this,
            subscribe.getPacketId(),
            ackReasonCodes
        ));
    }
}
