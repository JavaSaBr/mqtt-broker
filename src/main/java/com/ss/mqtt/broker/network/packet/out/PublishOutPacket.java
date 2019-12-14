package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.PacketType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class PublishOutPacket extends MqttWritablePacket implements HasPacketId {

    private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH.ordinal();

    protected final @Getter int packetId;

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }
}
