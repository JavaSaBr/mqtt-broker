package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * Publish acknowledgment (QoS 1).
 */
public class PublishAckInPacket extends MqttReadablePacket {

    private static final int PACKET_TYPE = PacketType.PUBLISH_ACK.ordinal();

    public PublishAckInPacket(byte info) {
        super(info);
    }

    @Override
    public byte getPacketType() {
        return (byte) PACKET_TYPE;
    }
}
