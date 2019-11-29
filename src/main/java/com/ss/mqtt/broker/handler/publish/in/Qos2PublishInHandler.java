package com.ss.mqtt.broker.handler.publish.in;

import com.ss.mqtt.broker.handler.publish.out.PublishOutHandler;
import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.reason.code.PublishCompletedReasonCode;
import com.ss.mqtt.broker.model.reason.code.PublishReceivedReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.network.packet.in.PublishReleaseInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import org.jetbrains.annotations.NotNull;

public class Qos2PublishInHandler extends AbstractPublishInHandler implements MqttSession.PendingPacketHandler {

    public Qos2PublishInHandler(
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

        // if this packet is re-try from client
        if (packet.isDuplicate()) {
            // if this packet was accepted before then we can skip it
            if (session.hasInPending(packet.getPacketId())) {
                return;
            }
        }

        super.handle(client, packet);
    }

    @Override
    protected void handleResult(
        @NotNull MqttClient client,
        @NotNull PublishInPacket packet,
        @NotNull ActionResult result
    ) {

        // because it was checked
        final MqttSession session = client.getSession();

        // it means this client was already closed
        if (session == null) {
            return;
        }

        PublishReceivedReasonCode reasonCode;

        switch (result) {
            case EMPTY:
                reasonCode = PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS;
                break;
            case SUCCESS:
                reasonCode = PublishReceivedReasonCode.SUCCESS;
                break;
            default:
                reasonCode = PublishReceivedReasonCode.UNSPECIFIED_ERROR;
                break;
        }

        session.registerInPublish(packet, this, packet.getPacketId());

        client.send(client.getPacketOutFactory().newPublishReceived(
            packet.getPacketId(),
            reasonCode
        ));
    }

    @Override
    public boolean handleResponse(@NotNull MqttClient client, @NotNull HasPacketId response) {

        if (!(response instanceof PublishReleaseInPacket)) {
            throw new IllegalStateException("Unexpected response " + response);
        }

        var packetOutFactory = client.getPacketOutFactory();
        client.send(packetOutFactory.newPublishCompleted(
            response.getPacketId(),
            PublishCompletedReasonCode.SUCCESS
        ));

        return true;
    }
}
