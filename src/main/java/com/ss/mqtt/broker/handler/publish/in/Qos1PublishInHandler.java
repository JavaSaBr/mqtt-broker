package com.ss.mqtt.broker.handler.publish.in;

import com.ss.mqtt.broker.handler.publish.out.PublishOutHandler;
import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.reason.code.PublishAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import org.jetbrains.annotations.NotNull;

public class Qos1PublishInHandler extends AbstractPublishInHandler {

    public Qos1PublishInHandler(
        @NotNull SubscriptionService subscriptionService,
        @NotNull PublishOutHandler[] publishOutHandlers
    ) {
        super(subscriptionService, publishOutHandlers);
    }

    @Override
    protected void handleImpl(
        @NotNull MqttClient client,
        @NotNull PublishInPacket packet,
        @NotNull ActionResult result
    ) {
        PublishAckReasonCode reasonCode = switch (result) {
            case EMPTY -> PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS;
            case SUCCESS -> PublishAckReasonCode.SUCCESS;
            default -> PublishAckReasonCode.UNSPECIFIED_ERROR;
        };
        var ackPacket = client.getPacketOutFactory().newPublishAck(
            client,
            packet.getPacketId(),
            reasonCode
        );
        client.send(ackPacket);
    }
}
