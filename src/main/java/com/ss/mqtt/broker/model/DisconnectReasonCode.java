package com.ss.mqtt.broker.model;

import com.ss.rlib.common.util.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@RequiredArgsConstructor
public enum DisconnectReasonCode {
    /**
     * Close the connection normally. Do not send the Will Message.
     * Client or Server.
     */
    NORMAL_DISCONNECTION((byte) 0x00),
    /**
     * The Client wishes to disconnect but requires that the Server also publishes its Will Message.
     * Client.
     */
    DISCONNECT_WITH_WILL_MESSAGE((byte) 0x04),

    // ERRORS

    /**
     * The Connection is closed but the sender either does not wish to reveal the reason, or none of
     * the other Reason Codes apply.
     * Client or Server.
     */
    UNSPECIFIED_ERROR((byte) 0x80),
    /**
     * The received packet does not conform to this specification.
     * Client or Server.
     */
    MALFORMED_PACKET((byte) 0x81),
    /**
     * An unexpected or out of order packet was received.
     * Client or Server.
     */
    PROTOCOL_ERROR((byte) 0x82),
    /**
     * The packet received is valid but cannot be processed by this implementation.
     * Client or Server.
     */
    IMPLEMENTATION_SPECIFIC_ERROR((byte) 0x83),
    /**
     * The request is not authorized.
     * Server.
     */
    NOT_AUTHORIZED((byte) 0x87),
    /**
     * The Server is busy and cannot continue processing requests from this Client.
     * Server.
     */
    SERVER_BUSY((byte) 0x89),
    /**
     * The Server is shutting down.
     * Server.
     */
    SERVER_SHUTTING_DOWN((byte) 0x8B),
    /**
     * The Connection is closed because no packet has been received for 1.5 times the Keepalive time.
     * Server.
     */
    KEEP_ALIVE_TIMEOUT((byte) 0x8D),
    /**
     * Another Connection using the same ClientID has connected causing this Connection to be closed.
     * Server.
     */
    SESSION_TAKEN_OVER((byte) 0x8E),
    /**
     * The Topic Filter is correctly formed, but is not accepted by this Sever.
     * Server.
     */
    TOPIC_FILTER_INVALID((byte) 0x8F),
    /**
     * The Topic Name is correctly formed, but is not accepted by this Client or Server.
     * Client or Server.
     */
    TOPIC_NAME_INVALID((byte) 0x90),
    /**
     * The Client or Server has received more than Receive Maximum publication for which it has
     * not sent PUBACK or PUBCOMP.
     * Client or Server.
     */
    RECEIVE_MAXIMUM_EXCEEDED((byte) 0x93),
    /**
     * The Client or Server has received a PUBLISH packet containing a Topic Alias which is greater
     * than the Maximum Topic Alias it sent in the CONNECT or CONNACK packet.
     * Client or Server.
     */
    TOPIC_ALIAS_INVALID((byte) 0x94),
    /**
     * The packet size is greater than Maximum Packet Size for this Client or Server.
     * Client or Server.
     */
    PACKET_TOO_LARGE((byte) 0x95),
    /**
     * The received data rate is too high.
     * Client or Server.
     */
    MESSAGE_RATE_TOO_HIGH((byte) 0x96),
    /**
     * An implementation or administrative imposed limit has been exceeded.
     * Client or Server.
     */
    QUOTA_EXCEEDED((byte) 0x97),
    /**
     * The Connection is closed due to an administrative action.
     * Client or Server.
     */
    ADMINISTRATIVE_ACTION((byte) 0x98),
    /**
     * The payload format does not match the one specified by the Payload Format Indicator.
     * Client or Server.
     */
    PAYLOAD_FORMAT_INVALID((byte) 0x99),
    /**
     * The Server has does not support retained messages.
     * Server.
     */
    RETAIN_NOT_SUPPORTED((byte) 0x9A),
    /**
     * The Client specified a QoS greater than the QoS specified in a Maximum QoS in the CONNACK.
     * Server.
     */
    QOS_NOT_SUPPORTED((byte) 0x9B),
    /**
     * The Client should temporarily change its Server.
     * Server.
     */
    USE_ANOTHER_SERVER((byte) 0x9C),
    /**
     * The Server is moved and the Client should permanently change its server location.
     * Server.
     */
    SERVER_MOVED((byte) 0x9D),
    /**
     * The Server does not support Shared Subscriptions.
     * Server.
     */
    SHARED_SUBSCRIPTIONS_NOT_SUPPORTED((byte) 0x9E),
    /**
     * This connection is closed because the connection rate is too high.
     * Server.
     */
    CONNECTION_RATE_EXCEEDED((byte) 0x9F),
    /**
     * The maximum connection time authorized for this connection has been exceeded.
     * Server.
     */
    MAXIMUM_CONNECT_TIME((byte) 0xA0),
    /**
     * The Server does not support Subscription Identifiers; the subscription is not accepted.
     * Server.
     */
    SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED((byte) 0xA1),
    /**
     * The Server does not support Wildcard Subscriptions; the subscription is not accepted.
     * Server.
     */
    WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED((byte) 0xA2);

    private static final DisconnectReasonCode[] VALUES;

    static {

        var maxId = Stream.of(values())
            .mapToInt(DisconnectReasonCode::getValue)
            .max()
            .orElse(0);

        var values = new DisconnectReasonCode[maxId + 1];

        for (var value : values()) {
            values[value.value] = value;
        }

        VALUES = values;
    }

    public static @NotNull DisconnectReasonCode of(int index) {
        return ObjectUtils.notNull(
            VALUES[index],
            index,
            arg -> new IndexOutOfBoundsException("Doesn't support reason code: " + arg)
        );
    }

    private @Getter final byte value;
}
