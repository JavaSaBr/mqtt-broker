package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.packet.PacketType;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Publish release (QoS 2 delivery part 2).
 */
@RequiredArgsConstructor
public class PublishRelease311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_RELEASED.ordinal();

    private final int packetId;

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected byte getPacketFlags() {
        return 2;
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
