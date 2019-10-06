package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.ConnectReturnCode;
import com.ss.mqtt.broker.network.packet.PacketType;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Connect acknowledgment.
 */
@RequiredArgsConstructor
public class ConnectAckOutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.CONNECT_ACK.ordinal();

    private final ConnectReturnCode connectReturnCode;
    private final boolean sessionPresent;

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    public int getExpectedLength() {
        return 2;
    }

    @Override
    protected void writeImpl(@NotNull ByteBuffer buffer) {
        super.writeImpl(buffer);

        buffer.put((byte) (sessionPresent ? 0x01 : 0x00));
        buffer.put(connectReturnCode.getValue());
    }
}
