package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * Publish release (QoS 2 delivery part 2).
 */
public class PublishReleaseInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_RELEASED.ordinal();

    public PublishReleaseInPacket(byte info) {
        super(info);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }
}
