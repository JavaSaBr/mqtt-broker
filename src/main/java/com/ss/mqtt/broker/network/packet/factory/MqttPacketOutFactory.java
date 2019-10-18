package com.ss.mqtt.broker.network.packet.factory;

import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.PublishAckReasonCode;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MqttPacketOutFactory {

    public abstract @NotNull MqttWritablePacket newConnectAck(
        @NotNull MqttClient client,
        @NotNull ConnectAckReasonCode reasonCode,
        boolean sessionPresent
    );

    public abstract @NotNull MqttWritablePacket newPublishAck(
        @NotNull MqttClient client,
        @NotNull PublishAckReasonCode reasonCode,
        int packetId,
        @Nullable String reason,
        @Nullable Array<StringPair> userProperties
    );
}
