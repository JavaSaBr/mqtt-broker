package com.ss.mqtt.broker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SubscribeAckReasonCode {
    /**
     * The subscription is accepted and the maximum QoS sent will be
     * QoS 0. This might be a lower QoS than was requested.
     */
    GRANTED_QOS_0((byte) 0x00),
    /**
     * The subscription is accepted and the maximum QoS sent will be
     * QoS 1. This might be a lower QoS than was requested.
     */
    GRANTED_QOS_1((byte) 0x01),
    /**
     * The subscription is accepted and any received QoS will be sent to
     * this subscription.
     */
    GRANTED_QOS_2((byte) 0x02),

    // ERRORS

    /**
     * The subscription is not accepted and the Server either does not
     * wish to reveal the reason or none of the other Reason Codes
     * apply.
     */
    UNSPECIFIED_ERROR((byte) 0x80),
    /**
     * The SUBSCRIBE is valid but the Server does not accept it.
     */
    IMPLEMENTATION_SPECIFIC_ERROR((byte) 0x83),
    /**
     * The Client is not authorized to make this subscription.
     */
    NOT_AUTHORIZED((byte) 0x87),
    /**
     * The Topic Filter is correctly formed but is not allowed for this Client.
     */
    TOPIC_FILTER_INVALID((byte) 0x8F),
    /**
     * The specified Packet Identifier is already in use.
     */
    PACKET_IDENTIFIER_IN_USE((byte) 0x91),
    /**
     * An implementation or administrative imposed limit has been
     * exceeded.
     */
    QUOTA_EXCEEDED((byte) 0x97),
    /**
     * The Server does not support Shared Subscriptions for this Client.
     */
    SHARED_SUBSCRIPTIONS_NOT_SUPPORTED((byte) 0x9E),
    /**
     * The Server does not support Subscription Identifiers; the subscription is not accepted.
     */
    SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED((byte) 0xA1),
    /**
     * The Server does not support Wildcard Subscriptions; the
     * subscription is not accepted.
     */
    WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED((byte) 0xA2);

    private @Getter final byte value;
}
