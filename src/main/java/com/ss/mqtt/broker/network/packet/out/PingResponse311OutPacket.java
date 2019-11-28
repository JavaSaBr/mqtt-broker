package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * PING response.
 */
public class PingResponse311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PING_RESPONSE.ordinal();

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }
}
