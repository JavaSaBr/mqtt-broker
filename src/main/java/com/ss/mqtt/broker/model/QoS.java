package com.ss.mqtt.broker.model;

import org.jetbrains.annotations.NotNull;

public enum QoS {
    AT_MOST_ONCE_DELIVERY,
    AT_LEAST_ONCE_DELIVERY,
    EXACTLY_ONCE_DELIVERY,
    INVALID;

    private static final QoS[] VALUES = values();

    public static @NotNull QoS of(int level) {
        if (level < 0 || level > EXACTLY_ONCE_DELIVERY.ordinal()) {
            return INVALID;
        } else {
            return VALUES[level];
        }
    }
}
