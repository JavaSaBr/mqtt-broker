package com.ss.mqtt.broker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

    private @Getter final byte value;
}
