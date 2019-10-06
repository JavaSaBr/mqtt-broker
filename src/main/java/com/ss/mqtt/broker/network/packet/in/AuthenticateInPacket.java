package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * Authentication exchange.
 */
public class AuthenticateInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.AUTHENTICATE.ordinal();

    protected AuthenticateInPacket(byte info) {
        super(info);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }
}
