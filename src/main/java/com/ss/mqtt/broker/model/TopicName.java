package com.ss.mqtt.broker.model;

import org.jetbrains.annotations.NotNull;

public class TopicName extends AbstractTopic {

    public static final TopicName EMPTY_TOPIC_NAME = new TopicName();

    private TopicName() {
    }

    public TopicName(@NotNull String topicName) {
        super(topicName);

        if (topicName.contains(MULTI_LEVEL_WILDCARD)) {
            throw new IllegalArgumentException("Multi level wildcard is incorrectly used: " + topicName);
        }
        if (topicName.contains(SINGLE_LEVEL_WILDCARD)) {
            throw new IllegalArgumentException("Single level wildcard is incorrectly used: " + topicName);
        }
    }

}

