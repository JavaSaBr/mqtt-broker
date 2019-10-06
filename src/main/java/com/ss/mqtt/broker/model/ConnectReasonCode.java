package com.ss.mqtt.broker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ConnectReasonCode {

    /**
     * The Connection is accepted.
     */
    SUCCESSFUL((byte) 0x00),

    // WITH REASONS BELOW SERVER MUST CLOSE CONNECTION

    /**
     * The Server does not wish to reveal the reason for the
     * failure, or none of the other Reason Codes apply.
     */
    UNSPECIFIED_ERROR((byte) 0x80),
    /**
     * Data within the CONNECT packet could not be
     * correctly parsed.
     */
    MALFORMED_PACKET((byte) 0x81),
    /**
     * Data in the CONNECT packet does not conform to this
     * specification.
     */
    PROTOCOL_ERROR((byte) 0x82),
    /**
     * The CONNECT is valid but is not accepted by this
     * Server.
     */
    IMPLEMENTATION_SPECIFIC_ERROR((byte) 0x83),
    /**
     * The Server does not support the version of the MQTT
     * protocol requested by the Client
     */
    UNSUPPORTED_PROTOCOL_VERSION((byte) 0x84),
    /**
     * The Client Identifier is a valid string but is not allowed
     * by the Server.
     */
    CLIENT_IDENTIFIER_NOT_VALID((byte) 0x85),
    /**
     * The Server does not accept the User Name or
     * Password specified by the Client
     */
    BAD_USER_NAME_OR_PASSWORD((byte) 0x86),
    /**
     * The Client is not authorized to connect.
     */
    NOT_AUTHORIZED((byte) 0x87),
    /**
     * The MQTT Server is not available
     */
    SERVER_UNAVAILABLE((byte) 0x88),
    /**
     * This Client has been banned by administrative action.
     * Contact the server administrator.
     */
    BANNED((byte) 0x88),
    /**
     * The authentication method is not supported or does not
     * match the authentication method currently in use.
     */
    BAD_AUTHENTICATION_METHOD((byte) 0x88),
    /**
     * The Will Topic Name is not malformed, but is not
     * accepted by this Server.
     */
    TOPIC_NAME_INVALID((byte) 0x88),
    /**
     * The CONNECT packet exceeded the maximum
     * permissible size.
     */
    PACKET_TOO_LARGE((byte) 0x88),
    /**
     * An implementation or administrative imposed limit has
     * been exceeded.
     */
    QUOTA_EXCEEDED((byte) 0x88),
    /**
     * The Will Payload does not match the specified Payload
     * Format Indicator.
     */
    PAYLOAD_FORMAT_INVALID((byte) 0x88),
    /**
     * The Server does not support retained messages, and
     * Will Retain was set to 1.
     */
    RETAIN_NOT_SUPPORTED((byte) 0x88),
    /**
     * The Server does not support the QoS set in Will QoS.
     */
    QOS_NOT_SUPPORTED((byte) 0x88),
    /**
     * The Client should temporarily use another server.
     */
    USE_ANOTHER_SERVER((byte) 0x88),
    /**
     * The Client should permanently use another server.
     */
    SERVER_MOVED((byte) 0x88),
    /**
     * The connection rate limit has been exceeded.
     */
    CONNECTION_RATE_EXCEEDED((byte) 0x88);

    private @Getter final byte value;
}
