package com.ss.mqtt.broker.model;

import com.ss.rlib.common.util.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum PublishReleaseReasonCode {

    /**
     * Message released.
     */
    SUCCESS((byte) 0x00),
    /**
     * The Packet Identifier is not known. This is not an error during recovery, but at other times indicates
     * a mismatch between the Session State on the Client and Server.
     */
    PACKET_IDENTIFIER_NOT_FOUND((byte) 0x92);

    private static final PublishReleaseReasonCode[] VALUES;

    static {

        var maxId = Stream.of(values())
            .mapToInt(PublishReleaseReasonCode::getValue)
            .map(value -> Byte.toUnsignedInt((byte) value))
            .max()
            .orElse(0);

        var values = new PublishReleaseReasonCode[maxId + 1];

        for (var value : values()) {
            values[Byte.toUnsignedInt(value.value)] = value;
        }

        VALUES = values;
    }

    public static @NotNull PublishReleaseReasonCode of(int index) {
        return ObjectUtils.notNull(
            VALUES[index],
            index,
            arg -> new IndexOutOfBoundsException("Doesn't support reason code: " + arg)
        );
    }

    private @Getter final byte value;
}
