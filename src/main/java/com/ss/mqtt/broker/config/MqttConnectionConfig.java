package com.ss.mqtt.broker.config;

import com.ss.mqtt.broker.model.QoS;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class MqttConnectionConfig {

    private final @NotNull QoS maxQos;

    private final int maximumPacketSize;

    private final boolean retainAvailable;
    private final boolean wildcardSubscriptionAvailable;
    private final boolean subscriptionIdAvailable;
    private final boolean sharedSubscriptionAvailable;
}
