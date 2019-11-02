package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

/**
 * PING request.
 */
public class PingRequest311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PING_REQUEST.ordinal();

    public PingRequest311OutPacket(@NotNull MqttClient client) {
        super(client);
    }

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }
}
