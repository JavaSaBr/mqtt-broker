package com.ss.mqtt.broker.model.reason.code;

import com.ss.rlib.common.util.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum UnsubscribeAckReasonCode {
    /**
     * The subscription is deleted.
     */
    SUCCESS((byte) 0x00),
    /**
     * The subscription is accepted and the maximum QoS sent will be
     * QoS 1. This might be a lower QoS than was requested.
     */
    NO_SUBSCRIPTION_EXISTED((byte) 0x11),

    // ERRORS

    /**
     * The unsubscribe could not be completed and the Server
     * either does not wish to reveal the reason or none of the
     * other Reason Codes apply.
     */
    UNSPECIFIED_ERROR((byte) 0x80),
    /**
     * The UNSUBSCRIBE is valid but the Server does not accept it.
     */
    IMPLEMENTATION_SPECIFIC_ERROR((byte) 0x83),
    /**
     * The Client is not authorized to unsubscribe.
     */
    NOT_AUTHORIZED((byte) 0x87),
    /**
     * The Topic Filter is correctly formed but is not allowed for this Client.
     */
    TOPIC_FILTER_INVALID((byte) 0x8F),
    /**
     * The specified Packet Identifier is already in use.
     */
    PACKET_IDENTIFIER_IN_USE((byte) 0x91);

    private static final UnsubscribeAckReasonCode[] VALUES;

    static {

        var maxId = Stream.of(values())
            .mapToInt(UnsubscribeAckReasonCode::getValue)
            .map(value -> Byte.toUnsignedInt((byte) value))
            .max()
            .orElse(0);

        var values = new UnsubscribeAckReasonCode[maxId + 1];

        for (var value : values()) {
            values[Byte.toUnsignedInt(value.value)] = value;
        }

        VALUES = values;
    }

    public static @NotNull UnsubscribeAckReasonCode of(int index) {
        return ObjectUtils.notNull(
            VALUES[index],
            index,
            arg -> new IndexOutOfBoundsException("Doesn't support reason code: " + arg)
        );
    }

    private @Getter final byte value;
}
