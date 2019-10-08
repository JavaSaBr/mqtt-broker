package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * Disconnect notification.
 */
public class DisconnectInPacket extends MqttReadablePacket {

    public static final byte PACKET_TYPE = (byte) PacketType.DISCONNECT.ordinal();

    public DisconnectInPacket(byte info) {
        super(info);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }
}