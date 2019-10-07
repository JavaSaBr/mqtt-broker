package com.ss.mqtt.broker.model;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public enum PacketProperty {
    PAYLOAD_FORMAT_INDICATOR(0x01, PacketDataType.BYTE),
    MESSAGE_EXPIRY_INTERVAL(0x02, PacketDataType.INTEGER),
    CONTENT_TYPE(0x03, PacketDataType.UTF_8_STRING),
    RESPONSE_TOPIC(0x08, PacketDataType.UTF_8_STRING),
    CORRELATION_DATA(0x09, PacketDataType.BINARY),
    SUBSCRIPTION_IDENTIFIER(0x0B, PacketDataType.MULTI_BYTE_INTEGER),
    SESSION_EXPIRY_INTERVAL(0x11, PacketDataType.INTEGER),
    ASSIGNED_CLIENT_IDENTIFIER(0x12, PacketDataType.UTF_8_STRING),
    SERVER_KEEP_ALIVE(0x13, PacketDataType.SHORT),
    AUTHENTICATION_METHOD(0x15, PacketDataType.UTF_8_STRING),
    AUTHENTICATION_DATA(0x16, PacketDataType.BINARY),
    REQUEST_PROBLEM_INFORMATION(0x17, PacketDataType.BYTE),
    WILL_DELAY_INTERVAL(0x18, PacketDataType.INTEGER),
    REQUEST_RESPONSE_INFORMATION(0x19, PacketDataType.BYTE),
    RESPONSE_INFORMATION(0x1A, PacketDataType.UTF_8_STRING),
    SERVER_REFERENCE(0x1C, PacketDataType.UTF_8_STRING),
    REASON_STRING(0x1F, PacketDataType.UTF_8_STRING),
    RECEIVE_MAXIMUM(0x21, PacketDataType.SHORT),
    TOPIC_ALIAS_MAXIMUM(0x22, PacketDataType.SHORT),
    TOPIC_ALIAS(0x23, PacketDataType.SHORT),
    MAXIMUM_QOS(0x24, PacketDataType.BYTE),
    RETAIN_AVAILABLE(0x25, PacketDataType.BYTE),
    USER_PROPERTY(0x26, PacketDataType.UTF_8_STRING_PAIR),
    MAXIMUM_PACKET_SIZE(0x27, PacketDataType.INTEGER),
    WILDCARD_SUBSCRIPTION_AVAILABLE(0x28, PacketDataType.BYTE),
    SUBSCRIPTION_IDENTIFIER_AVAILABLE(0x29, PacketDataType.BYTE),
    SHARED_SUBSCRIPTION_AVAILABLE(0x2A, PacketDataType.BYTE);

    private static final PacketProperty[] PROPERTIES;

    static {

        int maxId = Stream.of(values())
            .mapToInt(PacketProperty::getId)
            .max()
            .orElse(0);

        var result = new PacketProperty[maxId + 1];

        Stream.of(values())
            .forEach(prop -> result[prop.id] = prop);

        PROPERTIES = result;
    }

    public static @NotNull PacketProperty of(int id) {
        if (id < 0 || id >= PROPERTIES.length) {
            throw new IllegalArgumentException("Unknown property with id: " + id);
        } else {
            return PROPERTIES[id];
        }
    }

    private final @Getter byte id;
    private final @Getter PacketDataType dataType;

    PacketProperty(int id, @NotNull PacketDataType dataType) {
        this.id = (byte) id;
        this.dataType = dataType;
    }
}
