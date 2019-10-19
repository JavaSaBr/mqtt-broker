package com.ss.mqtt.broker.model;

public enum QoS {
    AT_MOST_ONCE_DELIVERY,
    AT_LEAST_ONCE_DELIVERY,
    EXACTLY_ONCE_DELIVERY,
    INVALID;
}
