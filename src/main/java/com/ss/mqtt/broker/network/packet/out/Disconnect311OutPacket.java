package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

/**
 * Disconnect notification.
 */
public class Disconnect311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.DISCONNECT.ordinal();

    public Disconnect311OutPacket(@NotNull MqttClient client) {
        super(client);
    }

    @Override
    public int getExpectedLength() {
        return 0;
    }

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }
}
