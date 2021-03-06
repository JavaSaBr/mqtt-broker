package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.util.DebugUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class Publish311OutPacket extends PublishOutPacket {

    static {
        DebugUtils.registerIncludedFields("qos", "topicName", "duplicate");
    }

    private final @NotNull QoS qos;
    private final @NotNull byte[] payload;
    private final @NotNull String topicName;

    private final boolean retained;
    private final boolean duplicate;

    public Publish311OutPacket(
        int packetId,
        @NotNull QoS qos,
        boolean retained,
        boolean duplicate,
        @NotNull String topicName,
        @NotNull byte[] payload
    ) {
        super(packetId);
        this.qos = qos;
        this.retained = retained;
        this.duplicate = duplicate;
        this.payload = payload;
        this.topicName = topicName;
    }

    @Override
    public int getExpectedLength() {
        return 7 + payload.length;
    }

    @Override
    protected byte getPacketFlags() {

        byte info = (byte) (qos.ordinal() << 1);

        if (retained) {
            info |= 0x01;
        }

        if (duplicate) {
            info |= 0x08;
        }

        return info;
    }

    @Override
    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
        // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc384800412
        writeString(buffer, topicName);
        if (qos.ordinal() > QoS.AT_MOST_ONCE.ordinal()) {
            writeShort(buffer, packetId);
        }
    }

    @Override
    protected void writePayload(@NotNull ByteBuffer buffer) {
        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc384800413
        buffer.put(payload);
    }
}
