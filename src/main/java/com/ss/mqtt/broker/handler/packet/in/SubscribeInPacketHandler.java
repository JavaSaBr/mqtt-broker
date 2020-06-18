package com.ss.mqtt.broker.handler.packet.in;

import static com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED;
import static com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED;
import static java.lang.Byte.toUnsignedInt;
import com.ss.mqtt.broker.model.reason.code.DisconnectReasonCode;
import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.SubscribeInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@RequiredArgsConstructor
public class SubscribeInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, SubscribeInPacket> {

    private final static Set<SubscribeAckReasonCode> INVALID_ACK_CODE = Set.of(
        SHARED_SUBSCRIPTIONS_NOT_SUPPORTED,
        WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED
    );

    private final @NotNull SubscriptionService subscriptionService;

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull SubscribeInPacket packet) {
        var ackReasonCodes = subscriptionService.subscribe(client, packet.getTopicFilters());
        client.send(client.getPacketOutFactory().newSubscribeAck(packet.getPacketId(), ackReasonCodes));
        var reason = ackReasonCodes.findAny(INVALID_ACK_CODE::contains);
        if (reason != null) {
            var disconnectReasonCode = DisconnectReasonCode.of(toUnsignedInt(reason.getValue()));
            var disconnect = client.getPacketOutFactory().newDisconnect(client, disconnectReasonCode);
            client.sendWithFeedback(disconnect).thenAccept(result -> client.getConnection().close());
        }
    }
}
