package com.ss.mqtt.broker.handler.publish.in;

import com.ss.mqtt.broker.handler.publish.out.PublishOutHandler;
import com.ss.mqtt.broker.service.SubscriptionService;

public class Qos0PublishInHandler extends AbstractPublishInHandler {

    public Qos0PublishInHandler(
        SubscriptionService subscriptionService,
        PublishOutHandler[] publishOutHandlers
    ) {
        super(subscriptionService, publishOutHandlers);
    }
}
