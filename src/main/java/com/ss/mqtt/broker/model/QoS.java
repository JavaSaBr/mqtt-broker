package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.model.reason.code.SubscribeAckReasonCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public enum QoS {
    AT_MOST_ONCE(SubscribeAckReasonCode.GRANTED_QOS_0),
    AT_LEAST_ONCE(SubscribeAckReasonCode.GRANTED_QOS_1),
    EXACTLY_ONCE(SubscribeAckReasonCode.GRANTED_QOS_2),
    INVALID(SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);

    private static final QoS[] VALUES = values();

    public static @NotNull QoS of(int level) {
        if (level < 0 || level > EXACTLY_ONCE.ordinal()) {
            return INVALID;
        } else {
            return VALUES[level];
        }
    }

    private final SubscribeAckReasonCode subscribeAckReasonCode;
}
