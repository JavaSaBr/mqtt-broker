package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * Publish complete (QoS 2 delivery part 3).
 */
public class PublishCompleteInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_COMPLETED.ordinal();

    protected PublishCompleteInPacket(byte info) {
        super(info);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }
}
