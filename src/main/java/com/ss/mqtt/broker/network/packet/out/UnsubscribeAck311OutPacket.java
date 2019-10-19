package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.network.MqttClient;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Unsubscribe acknowledgement.
 */
public class UnsubscribeAck311OutPacket extends MqttWritablePacket {

    private final int packetId;

    public UnsubscribeAck311OutPacket(@NotNull MqttClient client, int packetId) {
        super(client);
        this.packetId = packetId;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718074
        writeShort(buffer, packetId);
    }
}
