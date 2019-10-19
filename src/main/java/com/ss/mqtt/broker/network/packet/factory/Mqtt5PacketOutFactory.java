package com.ss.mqtt.broker.network.packet.factory;

import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.PublishAckReasonCode;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.out.ConnectAck5OutPacket;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.mqtt.broker.network.packet.out.PublishAck5OutPacket;
import com.ss.mqtt.broker.network.packet.out.SubscribeAck5OutPacket;
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
}
