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
    public void handle(@NotNull MqttClient client, @NotNull PublishInPacket packet) {

        var session = client.getSession();

        // it means this client was already closed
        if (session == null) {
            return;
        }

        super.handle(client, packet);
    }

    @Override
    protected void handleResult(
        @NotNull MqttClient client,
        @NotNull PublishInPacket packet,
        @NotNull ActionResult result
    ) {

        var reasonCode = switch (result) {
            case EMPTY -> PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS;
            case SUCCESS -> PublishAckReasonCode.SUCCESS;
            default -> PublishAckReasonCode.UNSPECIFIED_ERROR;
        };

        client.send(client.getPacketOutFactory().newPublishAck(
            client,
            packet.getPacketId(),
            reasonCode
        ));
    }
}
