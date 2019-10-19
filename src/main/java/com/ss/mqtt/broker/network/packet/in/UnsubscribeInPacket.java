package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.model.PacketProperty;
import com.ss.mqtt.broker.model.StringPair;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

/**
 * Unsubscribe request.
 */
@Getter
public class UnsubscribeInPacket extends MqttReadablePacket {

    private static final byte PACKET_TYPE = (byte) PacketType.UNSUBSCRIBE.ordinal();

    private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
          name is allowed to appear more than once.
         */
        PacketProperty.USER_PROPERTY
    );

    private @Nullable Array<StringPair> userProperties;
    private @NotNull Array<String> topicFilters;

    private int packetId;

    public UnsubscribeInPacket(byte info) {
        super(info);
        this.topicFilters = ArrayFactory.newArray(String.class);
    }

    @Override
    public byte getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    protected void readVariableHeader(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {
        packetId = readUnsignedShort(buffer);
    }

    @Override
    protected void readPayload(@NotNull MqttConnection connection, @NotNull ByteBuffer buffer) {

        if (buffer.remaining() < 1) {
            throw new IllegalStateException("No any topic filters.");
        }

        while (buffer.hasRemaining()) {
            topicFilters.add(readString(buffer));
        }
    }

    @Override
    protected @NotNull Set<PacketProperty> getAvailableProperties() {
        return AVAILABLE_PROPERTIES;
    }

    @Override
    protected void applyProperty(@NotNull PacketProperty property, @NotNull StringPair value) {
        switch (property) {
            case USER_PROPERTY:
                if (userProperties == null) {
                    userProperties = ArrayFactory.newArray(StringPair.class);
                }
                userProperties.add(value);
                break;
            default:
                unexpectedProperty(property);
        }
    }

    public @NotNull Array<StringPair> getUserProperties() {
        return ObjectUtils.ifNull(userProperties, Array.empty());
    }
}
