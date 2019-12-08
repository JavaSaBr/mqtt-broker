package com.ss.mqtt.broker.model;

import org.jetbrains.annotations.NotNull;

public enum SubscribeRetainHandling {
    /**
     * Send retained messages at the time of the subscribe.
     */
    SEND,
    /**
     * Send retained messages at subscribe only if the subscription does not currently exist.
     */
    SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST,
    /**
     * Do not send retained messages at the time of the subscribe.
     */
    DO_NOT_SEND,
    INVALID;

    private static final SubscribeRetainHandling[] VALUES = values();

    public static @NotNull SubscribeRetainHandling of(int level) {
        if (level < 0 || level > DO_NOT_SEND.ordinal()) {
            return SubscribeRetainHandling.INVALID;
        } else {
            return VALUES[level];
        }
    }
}
