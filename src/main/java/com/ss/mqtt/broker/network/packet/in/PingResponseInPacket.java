package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * PING response.
 */
public class PingResponseInPacket extends MqttReadablePacket {

    public static final byte PACKET_TYPE = (byte) PacketType.PING_RESPONSE.ordinal();

    public PingResponseInPacket(byte info) {
        super(info);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }
}
