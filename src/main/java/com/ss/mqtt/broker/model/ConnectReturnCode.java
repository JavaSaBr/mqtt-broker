package com.ss.mqtt.broker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ConnectReturnCode {
    ACCEPTED((byte) 0x00),
    REJECTED_UNACCEPTABLE_PROTOCOL_VERSION((byte) 0X01),
    REJECTED_REFUSED_IDENTIFIER_REJECTED((byte) 0x02),
    REJECTED_REFUSED_SERVER_UNAVAILABLE((byte) 0x03),
    REJECTED_REFUSED_BAD_USER_NAME_OR_PASSWORD((byte) 0x04),
    REJECTED_REFUSED_NOT_AUTHORIZED((byte) 0x05);

    private @Getter final byte value;
}
