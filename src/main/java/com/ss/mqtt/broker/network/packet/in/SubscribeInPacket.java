package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.packet.PacketType;

/**
 * Subscribe request.
 */
public class SubscribeInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.SUBSCRIBE.ordinal();

    public SubscribeInPacket(byte info) {
        super(info);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }
}
