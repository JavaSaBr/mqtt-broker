package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * PING request.
 */
public class PingRequest311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PING_REQUEST.ordinal();

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }
}
