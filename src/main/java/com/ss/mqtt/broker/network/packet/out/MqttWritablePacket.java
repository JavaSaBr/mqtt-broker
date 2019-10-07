package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.network.packet.impl.AbstractWritablePacket;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public abstract class MqttWritablePacket extends AbstractWritablePacket {

    private static final ThreadLocal<ByteBuffer> LOCAL_BUFFER =
        ThreadLocal.withInitial(() -> ByteBuffer.allocate(1024 * 1024));

    private final MqttClient client;

    public final int getPacketTypeAndFlags() {

        var type = getPacketType();
        var controlFlags = getPacketFlags();

        return NumberUtils.setHighByteBits(controlFlags, type);
    }

    protected byte getPacketType() {
        throw new UnsupportedOperationException();
    }

    protected byte getPacketFlags() {
        return 0;
    }

    protected int getExpectedPropertiesLength() {
        return 0;
    }

    protected void writeProperties(@NotNull ByteBuffer buffer, @NotNull ByteBuffer propertiesBuffer) {
        MqttDataUtils.writeMbi(propertiesBuffer.limit(), buffer);
        buffer.put(propertiesBuffer);
    }

    protected void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, byte value) {
        buffer
            .put(property.getId())
            .put(value);
    }

    protected void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, short value) {
        buffer
            .put(property.getId())
            .putShort(value);
    }

    protected void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, int value) {
        buffer
            .put(property.getId())
            .putInt(value);
    }

    protected void writeString(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, @NotNull String value) {

        var stringData = value.getBytes(StandardCharsets.UTF_8);

        buffer
            .put(property.getId())
            .putShort((short) stringData.length)
            .put(stringData);
    }

    protected void writeMbiProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, int value) {
        MqttDataUtils.writeMbi(value, buffer.put(property.getId()));
    }
}
