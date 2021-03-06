package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.mqtt.broker.util.DebugUtils;
import com.ss.rlib.common.util.array.Array;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Subscribe acknowledgement.
 */
@RequiredArgsConstructor
public class SubscribeAck311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.SUBSCRIBE_ACK.ordinal();

    static {
        DebugUtils.registerIncludedFields("reasonCodes", "packetId");
    }

    /**
     * The order of Reason Codes in the SUBACK packet MUST match the order of Topic Filters in the SUBSCRIBE packet.
     */
    private final @NotNull Array<SubscribeAckReasonCode> reasonCodes;

    /**
     * The Packet Identifier from the SUBSCRIBE.
     */
    private final int packetId;

    @Override
    public int getExpectedLength() {
        return 2 + reasonCodes.size();
    }

    @Override
    protected byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718070
        writeShort(buffer, packetId);
    }

    @Override
    protected void writePayload(@NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718071
        for (var reasonCode : reasonCodes) {
            writeByte(buffer, reasonCode.getValue());
        }
    }
}
