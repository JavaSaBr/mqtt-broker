package com.ss.mqtt.broker.model;

import com.ss.rlib.common.util.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum PublishAckReasonCode {
    /**
     * The message is accepted. Publication of the QoS 1
     * message proceeds.
     */
    SUCCESSFUL((byte) 0x00),
    /**
     * The message is accepted but there are no
     * subscribers. This is sent only by the Server. If the
     * Server knows that there are no matching subscribers,
     * it MAY use this Reason Code instead of 0x00
     * (Success).
     */
    NO_MATCHING_SUBSCRIBERS((byte) 0x10),

    // ERRORS

    /**
     * The receiver does not accept the publish but either
     * does not want to reveal the reason, or it does not
     * match one of the other values.
     */
    UNSPECIFIED_ERROR((byte) 0x80),
    /**
     * The PUBLISH is valid but the receiver is not willing to
     * accept it.
     */
    IMPLEMENTATION_SPECIFIC_ERROR((byte) 0x83),
    /**
     * The PUBLISH is not authorized.
     */
    NOT_AUTHORIZED((byte) 0x87),
    /**
     * The Topic Name is not malformed, but is not
     * accepted by this Client or Server.
     */
    TOPIC_NAME_INVALID((byte) 0x90),
    /**
     * The Packet Identifier is already in use. This might
     * indicate a mismatch in the Session State between the
     * Client and Server.
     */
    PACKET_IDENTIFIER_IN_USE((byte) 0x91),
    /**
     * An implementation or administrative imposed limit has
     * been exceeded.
     */
    QUOTA_EXCEEDED((byte) 0x97),
    /**
     * The payload format does not match the specified
     * Payload Format Indicator.
     */
    PAYLOAD_FORMAT_INVALID((byte) 0x99);

    private static final PublishAckReasonCode[] VALUES;

    static {

        var maxId = Stream.of(values())
            .mapToInt(PublishAckReasonCode::getValue)
            .map(value -> Byte.toUnsignedInt((byte) value))
            .max()
            .orElse(0);

        var values = new PublishAckReasonCode[maxId + 1];

        for (var value : values()) {
            values[Byte.toUnsignedInt(value.value)] = value;
        }

        VALUES = values;
    }

    public static @NotNull PublishAckReasonCode of(int index) {
        return ObjectUtils.notNull(
            VALUES[index],
            index,
            arg -> new IndexOutOfBoundsException("Doesn't support reason code: " + arg)
        );
    }

    private @Getter final byte value;
}
