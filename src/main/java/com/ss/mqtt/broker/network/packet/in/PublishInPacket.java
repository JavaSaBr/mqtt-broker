package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * Publish message.
 */
public class PublishInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH.ordinal();

    public PublishInPacket(byte info) {
        super(info);
        int qos = (info >> 1) & 0x03;
        boolean retained = (info & 0x01) == 0x01;
        boolean duplicate = (info & 0x08) == 0x08;
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }
}
