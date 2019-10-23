package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Publish release (QoS 2 delivery part 2).
 */
public class PublishRelease311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_RELEASED.ordinal();

    private final int packetId;

    public PublishRelease311OutPacket(@NotNull MqttClient client, int packetId) {
        super(client);
        this.packetId = packetId;
    }

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    public int getExpectedLength() {
        return 2;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718055
        writeShort(buffer, packetId);
    }
}
