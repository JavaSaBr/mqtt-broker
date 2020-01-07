package com.ss.mqtt.broker.handler.packet.in;

import static com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;
import com.ss.mqtt.broker.model.reason.code.DisconnectReasonCode;
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
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
        client.send(client.getPacketOutFactory().newSubscribeAck(packet.getPacketId(), ackReasonCodes));
        ackReasonCodes.findAny(client, SubscribeInPacketHandler::hasInvalidReason);
    }

    private static boolean hasInvalidReason(@NotNull UnsafeMqttClient client, @NotNull SubscribeAckReasonCode reason) {
        if (reason == SHARED_SUBSCRIPTIONS_NOT_SUPPORTED || reason == WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED) {
            int reasonIndex = Byte.toUnsignedInt(reason.getValue());
            var disconnect = client.getPacketOutFactory().newDisconnect(client, DisconnectReasonCode.of(reasonIndex));
            client.sendWithFeedback(disconnect).thenAccept(result -> client.getConnection().close());
            return true;
        } else {
            return false;
        }
    }
}
