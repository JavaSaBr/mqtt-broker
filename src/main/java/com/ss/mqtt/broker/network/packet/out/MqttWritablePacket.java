package com.ss.mqtt.broker.network.packet.out;

import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttClient;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.network.packet.impl.AbstractWritablePacket;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

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

    public void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, boolean value) {
        writeProperty(buffer, property, value ? 1 : 0);
    }

    public void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, long value, long def) {
        if (value != def) {
            writeProperty(buffer, property, value);
        }
    }

    public void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, long value) {

        buffer.put(property.getId());

        switch (property.getDataType()) {
            case BYTE:
                writeByte(buffer, (int) value);
                break;
            case SHORT:
                writeShort(buffer, (int) value);
                break;
            case INTEGER:
                writeInt(buffer, (int) value);
                break;
            case MULTI_BYTE_INTEGER:
                writeMbi(buffer, (int) value);
                break;
            default:
                throw new IllegalArgumentException("Incorrect property type: " + property);
        }
    }

    public void writeProperty(
        @NotNull ByteBuffer buffer,
        @NotNull PacketProperty property,
        @NotNull String value,
        @NotNull String def
    ) {

        if(!def.equals(value)) {
            writeProperty(buffer, property, value);
        }
    }

    public void writeProperty(
        @NotNull ByteBuffer buffer,
        @NotNull PacketProperty property,
        @NotNull StringPair value
    ) {

        buffer.put(property.getId());
        writeString(buffer, value.getName());
        writeString(buffer, value.getValue());
    }

    public void writeNotEmptyProperty(
        @NotNull ByteBuffer buffer,
        @NotNull PacketProperty property,
        @NotNull String value
    ) {

        if (!value.isEmpty()) {
            writeProperty(buffer, property, value);
        }
    }

    public void writeNotEmptyProperty(
        @NotNull ByteBuffer buffer,
        @NotNull PacketProperty property,
        @NotNull byte[] value
    ) {

        if (value.length > 0) {
            writeProperty(buffer, property, value);
        }
    }

    public void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, @NotNull String value) {
        buffer.put(property.getId());
        writeString(buffer, value);
    }

    public void writeProperty(@NotNull ByteBuffer buffer, @NotNull PacketProperty property, @NotNull byte[] value) {
        buffer.put(property.getId());
        writeBytes(buffer, value);
    }

    public void writeUserProperties(
        @NotNull ByteBuffer buffer,
        @NotNull Array<StringPair> userProperties
    ) {

        if (userProperties.isEmpty()) {
            return;
        }

        for (var pair : userProperties) {
            buffer.put(PacketProperty.USER_PROPERTY.getId());
            writeStringPair(buffer, pair);
        }
    }

    public void writeStringPairProperties(
        @NotNull ByteBuffer buffer,
        @NotNull PacketProperty property,
        @NotNull Array<StringPair> pairs
    ) {

        if (pairs.isEmpty()) {
            return;
        }

        buffer.put(property.getId());

        for (var pair : pairs) {
            writeStringPair(buffer, pair);
        }
    }

    @Override
    public void writeString(@NotNull ByteBuffer buffer, @NotNull String string) {
        var bytes = string.getBytes(StandardCharsets.UTF_8);
        buffer.putShort((short) bytes.length);
        buffer.put(bytes);
    }

    public void writeStringPair(@NotNull ByteBuffer buffer, @NotNull StringPair pair) {
        writeString(buffer, pair.getName());
        writeString(buffer, pair.getValue());
    }

    public void writeMbi(@NotNull ByteBuffer buffer, int value) {
        MqttDataUtils.writeMbi(value, buffer);
    }

    public void writeBytes(@NotNull ByteBuffer buffer, @NotNull byte[] bytes) {
        buffer.putShort((short) bytes.length);
        buffer.put(bytes);
    }
}
