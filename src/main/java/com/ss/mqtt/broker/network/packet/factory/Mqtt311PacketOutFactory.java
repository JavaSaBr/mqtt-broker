package com.ss.mqtt.broker.network.packet.factory;

import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.PublishAckReasonCode;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.out.ConnectAck311OutPacket;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.mqtt.broker.network.packet.out.PublishAck311OutPacket;
import com.ss.mqtt.broker.network.packet.out.SubscribeAck311OutPacket;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Mqtt311PacketOutFactory extends MqttPacketOutFactory {

    @Override
    public @NotNull MqttWritablePacket newConnectAck(
        @NotNull MqttClient client,
        @NotNull ConnectAckReasonCode reasonCode,
        boolean sessionPresent
    ) {
        return new ConnectAck311OutPacket(client, reasonCode, sessionPresent);
    }

    @Override
    public @NotNull MqttWritablePacket newPublishAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishAckReasonCode reasonCode,
        @Nullable String reason,
        @Nullable Array<StringPair> userProperties
    ) {
        return new PublishAck311OutPacket(client, packetId);
    }

    @Override
    public @NotNull MqttWritablePacket newSubscribeAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<SubscribeAckReasonCode> reasonCodes,
        @Nullable String reason,
        @Nullable Array<StringPair> userProperties
    ) {
        return new SubscribeAck311OutPacket(client, packetId, reasonCodes);
    }
}
