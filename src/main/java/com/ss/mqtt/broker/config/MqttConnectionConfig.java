package com.ss.mqtt.broker.config;

import com.ss.mqtt.broker.model.QoS;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class MqttConnectionConfig {

    private final @NotNull QoS maxQos;

    private final int maximumPacketSize;
    private final int minKeepAliveTime;
    private final int receiveMaximum;
    private final int topicAliasMaximum;

    private final long defaultSessionExpiryInterval;

    private final boolean keepAliveEnabled;
    private final boolean sessionsEnabled;
    private final boolean retainAvailable;
    private final boolean wildcardSubscriptionAvailable;
    private final boolean subscriptionIdAvailable;
    private final boolean sharedSubscriptionAvailable;


    @NoArgsConstructor @Data class MyClass { final String field = "";}
}
