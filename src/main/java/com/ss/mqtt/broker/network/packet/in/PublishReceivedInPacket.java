package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * Publish received (QoS 2 delivery part 1).
 */
public class PublishReceivedInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_RECEIVED.ordinal();

    protected PublishReceivedInPacket(byte info) {
        super(info);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }
}
