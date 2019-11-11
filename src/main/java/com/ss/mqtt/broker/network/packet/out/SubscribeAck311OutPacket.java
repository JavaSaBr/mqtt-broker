package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Subscribe acknowledgement.
 */
public class SubscribeAck311OutPacket extends MqttWritablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.SUBSCRIBE_ACK.ordinal();

    /**
     * The order of Reason Codes in the SUBACK packet MUST match the order of Topic Filters in the SUBSCRIBE packet.
     */
    private final @NotNull Array<SubscribeAckReasonCode> reasonCodes;

    /**
     * The Packet Identifier from the SUBSCRIBE.
     */
    private final int packetId;

    public SubscribeAck311OutPacket(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<SubscribeAckReasonCode> reasonCodes
    ) {
        super(client);
        this.reasonCodes = reasonCodes;
        this.packetId = packetId;
    }

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
