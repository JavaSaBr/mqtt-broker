package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.packet.PacketType;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Unsubscribe acknowledgement.
 */
@RequiredArgsConstructor
public class UnsubscribeAck311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.UNSUBSCRIBE_ACK.ordinal();

    private final int packetId;

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
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718074
        writeShort(buffer, packetId);
    }
}
