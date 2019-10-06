package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.ConnectReasonCode;
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

    private final ConnectReasonCode connectReasonCode;

    /**
     * The Session Present flag informs the Client whether the Server is using Session State from a
     * previous connection for this ClientID.
     * This allows the Client and Server to have a consistent view of the Session State.
     * If the Server accepts a connection with Clean Start set to 1, the Server MUST set Session
     * Present to 0 in the CONNACK packet in addition to setting a 0x00 (Success) Reason Code in the CONNACK packet
     */
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

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901077
        buffer.put((byte) (sessionPresent ? 0x01 : 0x00));
        buffer.put(connectReasonCode.getValue());
    }
}
