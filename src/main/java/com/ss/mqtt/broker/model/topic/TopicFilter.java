package com.ss.mqtt.broker.model.topic;

import org.jetbrains.annotations.NotNull;

public class TopicFilter extends AbstractTopic {

    public static TopicFilter from(@NotNull String topicFilter) {
        checkTopic(topicFilter);
        int multiPos = topicFilter.indexOf(MULTI_LEVEL_WILDCARD);
        if (multiPos != -1 && multiPos != topicFilter.length() - 1) {
            throw new IllegalArgumentException("Multi level wildcard is incorrectly used: " + topicFilter);
        } else if (topicFilter.contains("++")) {
            throw new IllegalArgumentException("Single level wildcard is incorrectly used: " + topicFilter);
        }
        return new TopicFilter(topicFilter);
    }

    private TopicFilter(@NotNull String topicFilter) {
        super(topicFilter);
    }
}

