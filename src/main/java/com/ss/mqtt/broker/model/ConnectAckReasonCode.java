package com.ss.mqtt.broker.model;

import com.ss.rlib.common.util.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum ConnectAckReasonCode {
    /**
     * The Connection is accepted.
     */
    SUCCESSFUL((byte) 0x00, (byte) 0x00),

    // WITH REASONS BELOW SERVER MUST CLOSE CONNECTION

    /**
     * The Server does not wish to reveal the reason for the
     * failure, or none of the other Reason Codes apply.
     */
    UNSPECIFIED_ERROR((byte) 0x01, (byte) 0x80),
    /**
     * Data within the CONNECT packet could not be
     * correctly parsed.
     */
    MALFORMED_PACKET((byte) 0x01, (byte) 0x81),
    /**
     * Data in the CONNECT packet does not conform to this
     * specification.
     */
    PROTOCOL_ERROR((byte) 0x01, (byte) 0x82),
    /**
     * The CONNECT is valid but is not accepted by this
     * Server.
     */
    IMPLEMENTATION_SPECIFIC_ERROR((byte) 0x01, (byte) 0x83),
    /**
     * The Server does not support the version of the MQTT
     * protocol requested by the Client
     */
    UNSUPPORTED_PROTOCOL_VERSION((byte) 0x01, (byte) 0x84),
    /**
     * The Client Identifier is a valid string but is not allowed
     * by the Server.
     */
    CLIENT_IDENTIFIER_NOT_VALID((byte) 0x02, (byte) 0x85),
    /**
     * The Server does not accept the User Name or
     * Password specified by the Client
     */
    BAD_USER_NAME_OR_PASSWORD((byte) 0x04, (byte) 0x86),
    /**
     * The Client is not authorized to connect.
     */
    NOT_AUTHORIZED((byte) 0x05, (byte) 0x87),
    /**
     * The MQTT Server is not available
     */
    SERVER_UNAVAILABLE((byte) 0x03, (byte) 0x88),
    /**
     * The Server is busy. Try again later.
     */
    SERVER_BUSY((byte) 0x01, (byte) 0x89),
    /**
     * This Client has been banned by administrative action.
     * Contact the server administrator.
     */
    BANNED((byte) 0x01, (byte) 0x8A),
    /**
     * The authentication method is not supported or does not
     * match the authentication method currently in use.
     */
    BAD_AUTHENTICATION_METHOD((byte) 0x04, (byte) 0x8C),
    /**
     * The Will Topic Name is not malformed, but is not
     * accepted by this Server.
     */
    TOPIC_NAME_INVALID((byte) 0x01, (byte) 0x90),
    /**
     * The CONNECT packet exceeded the maximum
     * permissible size.
     */
    PACKET_TOO_LARGE((byte) 0x01, (byte) 0x95),
    /**
     * An implementation or administrative imposed limit has
     * been exceeded.
     */
    QUOTA_EXCEEDED((byte) 0x01, (byte) 0x97),
    /**
     * The Will Payload does not match the specified Payload
     * Format Indicator.
     */
    PAYLOAD_FORMAT_INVALID((byte) 0x01, (byte) 0x99),
    /**
     * The Server does not support retained messages, and
     * Will Retain was set to 1.
     */
    RETAIN_NOT_SUPPORTED((byte) 0x01, (byte) 0x9A),
    /**
     * The Server does not support the QoS set in Will QoS.
     */
    QOS_NOT_SUPPORTED((byte) 0x01, (byte) 0x9B),
    /**
     * The Client should temporarily use another server.
     */
    USE_ANOTHER_SERVER((byte) 0x01, (byte) 0x9C),
    /**
     * The Client should permanently use another server.
     */
    SERVER_MOVED((byte) 0x01, (byte) 0x9D),
    /**
     * The connection rate limit has been exceeded.
     */
    CONNECTION_RATE_EXCEEDED((byte) 0x01, (byte) 0x9F);

    private static final ConnectAckReasonCode[] MQTT_5_VALUES;

    static {

        var maxId = Stream.of(values())
            .mapToInt(ConnectAckReasonCode::getMqtt5)
            .map(value -> Byte.toUnsignedInt((byte) value))
            .max()
            .orElse(0);

        var values = new ConnectAckReasonCode[maxId + 1];

        for (var value : values()) {
            values[Byte.toUnsignedInt(value.mqtt5)] = value;
        }

        MQTT_5_VALUES = values;
    }

    public static @NotNull ConnectAckReasonCode of(boolean mqtt5, int reasonCode) {
        return mqtt5 ? ofMqtt5(reasonCode) : ofMqtt311(reasonCode);
    }

    public static @NotNull ConnectAckReasonCode ofMqtt311(int reasonCode) {
        switch (reasonCode) {
            case 0x00:
                return SUCCESSFUL;
            case 0x01:
                return UNSUPPORTED_PROTOCOL_VERSION;
            case 0x02:
                return CLIENT_IDENTIFIER_NOT_VALID;
            case 0x03:
                return SERVER_UNAVAILABLE;
            case 0x04:
                return BAD_USER_NAME_OR_PASSWORD;
            case 0x05:
                return NOT_AUTHORIZED;
            default:
                throw new IllegalArgumentException("Unsupported reason code: " + reasonCode);
        }
    }

    public static @NotNull ConnectAckReasonCode ofMqtt5(int reasonCode) {
        return ObjectUtils.notNull(
            MQTT_5_VALUES[reasonCode],
            reasonCode,
            arg -> new IndexOutOfBoundsException("Doesn't support reason code: " + arg)
        );
    }

    private @Getter final byte mqtt311;
    private @Getter final byte mqtt5;

}
