package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.exception.ConnectionRejectException;
import com.ss.mqtt.broker.exception.MqttException;
import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.network.packet.impl.AbstractReadablePacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

public abstract class MqttReadablePacket extends AbstractReadablePacket<MqttConnection> {

    /**
     * The list of user properties.
     */
    protected @Getter @NotNull Array<StringPair> userProperties;

    /**
     * The happened exception during parsing this packet.
     */
    protected @Getter @Nullable Exception exception;

    protected MqttReadablePacket(byte info) {
        this.userProperties = Array.empty();
    }

    public abstract byte getPacketType();

    @Override
    protected void readImpl(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        readVariableHeader(connection, buffer);

        if (isPropertiesSupported(connection, buffer)) {
            readProperties(buffer);
        }

        readPayload(connection, buffer);
    }

    @Override
    protected void handleException(@NotNull ByteBuffer buffer, @NotNull Exception exception) {
        super.handleException(buffer, exception);

        if (!(exception instanceof MqttException)) {
            exception = new ConnectionRejectException(exception, ConnectAckReasonCode.PROTOCOL_ERROR);
        }

        this.exception = exception;
    }

    protected boolean isPropertiesSupported(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        return connection.isSupported(MqttVersion.MQTT_5);
    }

    protected void readVariableHeader(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
    }

    protected void readProperties(@NotNull ByteBuffer buffer) {
        readProperties(buffer, getAvailableProperties());
    }

    protected void readPayload(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
    }

    protected void readProperties(@NotNull ByteBuffer buffer, @NotNull Set<PacketProperty> availableProperties) {

        var propertiesLength = MqttDataUtils.readMbi(buffer);

        if (propertiesLength == -1) {
            throw new IllegalStateException("Can't read properties length.");
        } else if(propertiesLength == 0) {
            return;
        }

        var lastPositionInBuffer = buffer.position() + propertiesLength;

        while (buffer.position() < lastPositionInBuffer) {

            var property = PacketProperty.of(readUnsignedByte(buffer));

            if (!availableProperties.contains(property)) {
                throw new IllegalStateException("Property: " + property + " is not available for this packet.");
            }

            switch (property.getDataType()) {
                case BYTE:
                    applyProperty(property, readUnsignedByte(buffer));
                    break;
                case SHORT:
                    applyProperty(property, readUnsignedShort(buffer));
                    break;
                case INTEGER:
                    applyProperty(property, readUnsignedInt(buffer));
                    break;
                case MULTI_BYTE_INTEGER:
                    applyProperty(property, MqttDataUtils.readMbi(buffer));
                    break;
                case UTF_8_STRING:
                    applyProperty(property, readString(buffer));
                    break;
                case UTF_8_STRING_PAIR:
                    applyProperty(property, new StringPair(readString(buffer), readString(buffer)));
                    break;
                case BINARY:
                    applyProperty(property, readBytes(buffer));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported data type: " + property.getDataType());
            }
        }
    }

    protected @NotNull Set<PacketProperty> getAvailableProperties() {
        return Collections.emptySet();
    }

    protected void applyProperty(@NotNull PacketProperty property, long value) {
    }

    protected void applyProperty(@NotNull PacketProperty property, @NotNull String value) {
    }

    protected void applyProperty(@NotNull PacketProperty property, @NotNull byte[] value) {
    }

    protected void applyProperty(@NotNull PacketProperty property, @NotNull StringPair value) {
        switch (property) {
            case USER_PROPERTY:
                if (userProperties == Array.<StringPair>empty()) {
                    userProperties = ArrayFactory.newArray(StringPair.class);
                }
                userProperties.add(value);
                break;
        }
    }

    protected int readUnsignedByte(@NotNull ByteBuffer buffer) {
        return Byte.toUnsignedInt(buffer.get());
    }

    protected int readUnsignedShort(@NotNull ByteBuffer buffer) {
        return Short.toUnsignedInt(buffer.getShort());
    }

    protected long readUnsignedInt(@NotNull ByteBuffer buffer) {
        return Integer.toUnsignedLong(buffer.getInt());
    }

    @Override
    protected @NotNull String readString(@NotNull ByteBuffer buffer) {
        var stringData = new byte[readShort(buffer) & 0xFFFF];
        buffer.get(stringData);
        return new String(stringData, StandardCharsets.UTF_8);
    }

    protected @NotNull byte[] readBytes(@NotNull ByteBuffer buffer) {
        var data = new byte[readShort(buffer) & 0xFFFF];
        buffer.get(data);
        return data;
    }

    protected @NotNull byte[] readPayload(@NotNull ByteBuffer buffer) {

        var payloadSize = buffer.limit() - buffer.position();

        if (payloadSize < 1) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        var data = new byte[payloadSize];
        buffer.get(data);
        return data;
    }

    protected void unexpectedProperty(@NotNull PacketProperty property) {
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
}
