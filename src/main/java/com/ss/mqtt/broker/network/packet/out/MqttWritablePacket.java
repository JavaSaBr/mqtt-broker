package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.network.packet.impl.AbstractWritablePacket;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public abstract class MqttWritablePacket extends AbstractWritablePacket {

    private static final ThreadLocal<ByteBuffer> LOCAL_BUFFER =
        ThreadLocal.withInitial(() -> ByteBuffer.allocate(1024 * 1024));

    protected final MqttClient client;

    @Override
    protected void writeImpl(@NotNull ByteBuffer buffer) {
        writeVariableHeader(buffer);

        if (isPropertiesSupported()) {
            appendProperties(buffer);
        }

        writePayload(buffer);
    }

    protected void writeVariableHeader(@NotNull ByteBuffer buffer) {
    }

    protected void writePayload(@NotNull ByteBuffer buffer) {
    }

    protected boolean isPropertiesSupported() {
        return false;
    }

    protected void writeProperties(@NotNull ByteBuffer buffer) {
    }

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

    protected @NotNull ByteBuffer getPropertiesBuffer() {
        return LOCAL_BUFFER.get().clear();
    }

    private void appendProperties(@NotNull ByteBuffer buffer) {

        var propertiesBuffer = getPropertiesBuffer();

        writeProperties(propertiesBuffer);

        if (propertiesBuffer.position() < 1) {
            buffer.put((byte) 0);
            return;
        }

        propertiesBuffer.flip();

        MqttDataUtils.writeMbi(propertiesBuffer.limit(), buffer)
            .put(propertiesBuffer);
    }

    protected void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, long value, long def) {
        if (value != def) {
            writeProperty(buffer, property, value);
        }
    }

    protected void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, long value) {

        buffer.put(property.getId());

        switch (property.getDataType()) {
            case BYTE:
                buffer.put((byte) value);
                break;
            case SHORT:
                buffer.putShort((short) value);
                break;
            case INTEGER:
                buffer.putInt((int) value);
                break;
            case MULTI_BYTE_INTEGER:
                MqttDataUtils.writeMbi(value, buffer);
                break;
            default:
                throw new IllegalArgumentException("Incorrect property type: " + property);
        }
    }

    protected void writeProperty(
        @NotNull ByteBuffer buffer,
        @NotNull PacketProperty property,
        @NotNull String value,
        @NotNull String def
    ) {

        if(!def.equals(value)) {
            writeProperty(buffer, property, value);
        }
    }

    protected void writeNullableProperty(
        @NotNull ByteBuffer buffer,
        @NotNull PacketProperty property,
        @Nullable String value
    ) {

        if (value != null) {
            writeProperty(buffer, property, value);
        }
    }

    protected void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, @NotNull String value) {

        var stringData = value.getBytes(StandardCharsets.UTF_8);

        buffer
            .put(property.getId())
            .putShort((short) stringData.length)
            .put(stringData);
    }

    protected void writeProperty(
        @NotNull ByteBuffer buffer,
        @NotNull PacketProperty property,
        @NotNull StringPair value
    ) {

        var nameData = value.getName().getBytes(StandardCharsets.UTF_8);
        var valueData = value.getValue().getBytes(StandardCharsets.UTF_8);

        buffer
            .put(property.getId())
            .putShort((short) nameData.length)
            .put(nameData)
            .putShort((short) valueData.length)
            .put(valueData);
    }
}
