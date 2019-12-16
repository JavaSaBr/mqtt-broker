package com.ss.mqtt.broker.model.topic;

import org.jetbrains.annotations.NotNull;

public class TopicName extends AbstractTopic {

    public static final TopicName INVALID_TOPIC_NAME = new TopicName(true);
    public static final TopicName EMPTY_TOPIC_NAME = new TopicName(false);

    public static @NotNull TopicName from(@NotNull String topicName) {
        if (!checkTopic(topicName) ||
            topicName.contains(MULTI_LEVEL_WILDCARD) ||
            topicName.contains(SINGLE_LEVEL_WILDCARD)
        ) {
            return INVALID_TOPIC_NAME;
        } else {
            return new TopicName(topicName);
        }
    }

    private TopicName(@NotNull String topicName) {
        super(topicName);
    }

    private TopicName(boolean invalid) {
        super(invalid);
    }

}

