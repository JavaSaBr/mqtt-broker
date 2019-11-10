package com.ss.mqtt.broker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public enum QoS {
    AT_MOST_ONCE_DELIVERY(SubscribeAckReasonCode.GRANTED_QOS_0),
    AT_LEAST_ONCE_DELIVERY(SubscribeAckReasonCode.GRANTED_QOS_1),
    EXACTLY_ONCE_DELIVERY(SubscribeAckReasonCode.GRANTED_QOS_2),
    INVALID(SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);

    private static final QoS[] VALUES = values();

    public static @NotNull QoS of(int level) {
        if (level < 0 || level > EXACTLY_ONCE_DELIVERY.ordinal()) {
            return INVALID;
        } else {
            return VALUES[level];
        }
    }

    private final SubscribeAckReasonCode subscribeAckReasonCode;

    public QoS nextQos() {
        int nextLevel = ordinal() + 1;
        if (nextLevel < INVALID.ordinal()) {
            return VALUES[nextLevel];
        } else {
            return EXACTLY_ONCE_DELIVERY;
        }
    }
}
