package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

/**
 * PING response.
 */
public class PingResponse311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PING_RESPONSE.ordinal();

    public PingResponse311OutPacket(@NotNull MqttClient client) {
        super(client);
    }

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }
}
