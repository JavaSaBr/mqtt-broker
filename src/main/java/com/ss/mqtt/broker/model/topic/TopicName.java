package com.ss.mqtt.broker.model.topic;

import com.ss.mqtt.broker.util.TopicUtils;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class TopicName extends AbstractTopic {

    public static final TopicName INVALID_TOPIC_NAME = new TopicName();
    public static final TopicName EMPTY_TOPIC_NAME = new TopicName();

    public static @NotNull TopicName from(@NotNull String topicName) {
        if (!TopicUtils.check(topicName) ||
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
}

