package com.ss.mqtt.broker.model.reason.code;

import com.ss.rlib.common.util.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum AuthenticateReasonCode {

    /**
     * Authentication is successful.
     * Server.
     */
    SUCCESS((byte) 0x00),
    /**
     * Continue the authentication with another step.
     * Client or Server.
     */
    CONTINUE_AUTHENTICATION((byte) 0x18),
    /**
     * Initiate a re-authentication.
     * Client.
     */
    RE_AUTHENTICATE((byte) 0x19);

    private static final AuthenticateReasonCode[] VALUES;

    static {

        var maxId = Stream.of(values())
            .mapToInt(AuthenticateReasonCode::getValue)
            .max()
            .orElse(0);

        var values = new AuthenticateReasonCode[maxId + 1];

        for (var value : values()) {
            values[value.value] = value;
        }

        VALUES = values;
    }

    public static @NotNull AuthenticateReasonCode of(int index) {
        return ObjectUtils.notNull(
            VALUES[index],
            index,
            arg -> new IndexOutOfBoundsException("Doesn't support reason code: " + arg)
        );
    }

    private @Getter final byte value;
}
