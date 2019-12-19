package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.model.topic.TopicFilter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public abstract class AbstractSubscriber implements Subscriber {

    final @NotNull SubscribeTopicFilter subscribeTopicFilter;

    public @NotNull QoS getQos() {
        return subscribeTopicFilter.getQos();
    }

    public @NotNull TopicFilter getTopicFilter() {
        return subscribeTopicFilter.getTopicFilter();
    }
}
