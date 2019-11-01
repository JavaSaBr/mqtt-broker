package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Publish received (QoS 2 delivery part 1).
 */
public class PublishReceived311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_RECEIVED.ordinal();

    private final int packetId;

    public PublishReceived311OutPacket(@NotNull MqttClient client, int packetId) {
        super(client);
        this.packetId = packetId;
    }

    @Override
    public int getExpectedLength() {
        return 2;
    }

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718050
        writeShort(buffer, packetId);
    }
}
