package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.PublishAckReasonCode;
import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Publish acknowledgement.
 */
public class PublishAck311OutPacket extends MqttWritablePacket {

    private final @NotNull PublishAckReasonCode reasonCode;

    /**
     * Packet Identifier from the PUBLISH packet that is being acknowledged.
     */
    private final int packetId;

    public PublishAck311OutPacket(@NotNull MqttClient client, @NotNull PublishAckReasonCode reasonCode, int packetId) {
        super(client);
        this.reasonCode = reasonCode;
        this.packetId = packetId;
    }

    @Override
    public int getExpectedLength() {
        return 3;
    }

    @Override
    protected void writeImpl(@NotNull ByteBuffer buffer) {
        super.writeImpl(buffer);

        buffer.putShort((short) packetId);
        buffer.put(reasonCode.getValue());
    }
}
