package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * PING request.
 */
public class PingRequestInPacket extends MqttReadablePacket {

    public static final byte PACKET_TYPE = (byte) PacketType.PING_REQUEST.ordinal();

    public PingRequestInPacket(byte info) {
        super(info);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }
}
