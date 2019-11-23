package com.ss.mqtt.broker.handler.publish.in;

import com.ss.mqtt.broker.handler.publish.out.PublishOutHandler;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
abstract class AbstractPublishInHandler implements PublishInHandler {

    protected final @NotNull SubscriptionService subscriptionService;
    protected final @NotNull PublishOutHandler[] publishOutHandlers;

    protected @NotNull PublishOutHandler publishOutHandler(@NotNull QoS qos) {
        return publishOutHandlers[qos.ordinal()];
    }
}
