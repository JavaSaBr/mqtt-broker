package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Publish acknowledgement.
 */
public class PublishAck311OutPacket extends MqttWritablePacket {

    /**
     * Packet Identifier from the PUBLISH packet that is being acknowledged.
     */
    private final int packetId;

    public PublishAck311OutPacket(@NotNull MqttClient client, int packetId) {
        super(client);
        this.packetId = packetId;
    }

    @Override
    public int getExpectedLength() {
        return 2;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718045
        buffer.putShort((short) packetId);
    }
}
