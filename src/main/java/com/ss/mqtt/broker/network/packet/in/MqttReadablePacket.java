package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.network.packet.impl.AbstractReadablePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

public abstract class MqttReadablePacket extends AbstractReadablePacket<MqttConnection> {

    protected static final int MAX_MSB_LSB = 65535;

    protected MqttReadablePacket(byte info) {
    }

    public abstract byte getPacketType();

    protected void readProperties(@NotNull ByteBuffer buffer) {

        var propertiesLength = MqttDataUtils.readMbi(buffer);

        if (propertiesLength == -1) {
            throw new IllegalStateException("Can't read properties length.");
        } else if(propertiesLength == 0) {
            return;
        }

        var lastPositionInBuffer = buffer.position() + (int) propertiesLength;
        var properties = getAvailableProperties();

        while (buffer.position() < lastPositionInBuffer) {

            var property = PacketProperty.of(readUnsignedByte(buffer));

            if (!properties.contains(property)) {
                throw new IllegalStateException("Property: " + property + " is not available for this packet.");
            }

            switch (property.getDataType()) {
                case BYTE:
                    applyProperty(property, readByte(buffer));
                    break;
                case SHORT:
                    applyProperty(property, readShort(buffer));
                    break;
                case INTEGER:
                    applyProperty(property, readInt(buffer));
                    break;
                case MULTI_BYTE_INTEGER:
                    applyProperty(property, (int) MqttDataUtils.readMbi(buffer));
                    break;
                case UTF_8_STRING:
                    applyProperty(property, readString(buffer));
                    break;
                case BINARY:
                    applyProperty(property, readBinary(buffer));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported data type: " + property.getDataType());
            }
        }
    }

    protected @NotNull Set<PacketProperty> getAvailableProperties() {
        return Collections.emptySet();
    }

    protected void applyProperty(@NotNull PacketProperty property, int value) {
    }

    protected void applyProperty(@NotNull PacketProperty property, @NotNull String value) {
    }

    protected void applyProperty(@NotNull PacketProperty property, @NotNull byte[] value) {
    }

    protected int readUnsignedByte(@NotNull ByteBuffer buffer) {
        return NumberUtils.toUnsignedByte(buffer.get());
    }

    protected int readMsbLsbInt(@NotNull ByteBuffer buffer) {
        return readMsbLsbInt(buffer, 0, MAX_MSB_LSB);
    }

    protected int readMsbLsbInt(@NotNull ByteBuffer buffer, int min, int max) {

        var msbSize = readUnsignedByte(buffer);
        var lsbSize = readUnsignedByte(buffer);
        var result = msbSize << 8 | lsbSize;

        if (result < min || result > max) {
            result = -1;
        }

        return result;
    }

    protected @NotNull byte[] readBinary(@NotNull ByteBuffer buffer) {
        var data = new byte[readShort(buffer) & 0xFFFF];
        buffer.get(data);
        return data;
    }

    @Override
    protected @NotNull String readString(@NotNull ByteBuffer buffer) {
        var stringData = new byte[readMsbLsbInt(buffer)];
        buffer.get(stringData);
        return new String(stringData, StandardCharsets.UTF_8);
    }

    protected @Nullable String readString(@NotNull ByteBuffer buffer, int minBytes, int maxBytes) {

        var length = readMsbLsbInt(buffer);

        if (length < minBytes || length > maxBytes) {
            buffer.position(buffer.position() + length);
            return null;
        }

        var stringData = new byte[length];

        buffer.get(stringData);

        return new String(stringData, StandardCharsets.UTF_8);
    }

    protected @NotNull byte[] readBytes(@NotNull ByteBuffer buffer) {
        var data = new byte[readMsbLsbInt(buffer)];
        buffer.get(data);
        return data;
    }
}
