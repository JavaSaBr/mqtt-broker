package com.ss.mqtt.broker.model.topic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TopicName extends AbstractTopic {

    public static final TopicName EMPTY_TOPIC_NAME = new TopicName();

    public static @NotNull TopicName from(@NotNull String topicName) {
        checkTopic(topicName);
        if (topicName.contains(MULTI_LEVEL_WILDCARD)) {
            throw new IllegalArgumentException("Multi level wildcard is incorrectly used: " + topicName);
        } else if (topicName.contains(SINGLE_LEVEL_WILDCARD)) {
            throw new IllegalArgumentException("Single level wildcard is incorrectly used: " + topicName);
        }
        return new TopicName(topicName);
    }

    private TopicName(@NotNull String topicName) {
        super(topicName);
    }

}

