package com.ss.mqtt.broker.network.packet.factory;

import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.out.*;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Mqtt5PacketOutFactory extends Mqtt311PacketOutFactory {

    @Override
    public @NotNull MqttWritablePacket newConnectAck(
        @NotNull MqttClient client,
        @NotNull ConnectAckReasonCode reasonCode,
        boolean sessionPresent
    ) {
        return new ConnectAck5OutPacket(client, reasonCode, sessionPresent);
    }

    @Override
    public @NotNull MqttWritablePacket newPublishAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishAckReasonCode reasonCode,
        @Nullable String reason,
        @Nullable Array<StringPair> userProperties
    ) {
        return new PublishAck5OutPacket(client, packetId, reasonCode, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newSubscribeAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<SubscribeAckReasonCode> reasonCodes,
        @Nullable String reason,
        @Nullable Array<StringPair> userProperties
    ) {
        return new SubscribeAck5OutPacket(client, packetId, reasonCodes, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newUnsubscribeAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<UnsubscribeAckReasonCode> reasonCodes,
        @Nullable Array<StringPair> userProperties,
        @Nullable String reason
    ) {
        return new UnsubscribeAck5OutPacket(client, packetId, reasonCodes, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newDisconnect(
        @NotNull MqttClient client,
        @NotNull DisconnectReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @Nullable String reason,
        @Nullable String serverReference
    ) {
        return new Disconnect5OutPacket(client, reasonCode, userProperties, reason, serverReference);
    }
}
