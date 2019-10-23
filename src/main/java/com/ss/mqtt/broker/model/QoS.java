package com.ss.mqtt.broker.model;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public enum QoS {
    AT_MOST_ONCE_DELIVERY(SubscribeAckReasonCode.GRANTED_QOS_0),
    AT_LEAST_ONCE_DELIVERY(SubscribeAckReasonCode.GRANTED_QOS_1),
    EXACTLY_ONCE_DELIVERY(SubscribeAckReasonCode.GRANTED_QOS_2),
    INVALID(SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);

    private static final QoS[] VALUES = values();

    private final SubscribeAckReasonCode subscribeAckReasonCode;

    QoS(@NotNull SubscribeAckReasonCode subscribeAckReasonCode) {
        this.subscribeAckReasonCode = subscribeAckReasonCode;
    }

    public static @NotNull QoS of(int level) {
        if (level < 0 || level > EXACTLY_ONCE_DELIVERY.ordinal()) {
            return INVALID;
        } else {
            return VALUES[level];
        }
    }
}
