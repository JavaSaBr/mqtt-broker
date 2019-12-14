package com.ss.mqtt.broker.model.topic;

import org.jetbrains.annotations.NotNull;

public class TopicFilter extends AbstractTopic {

    public static @NotNull TopicFilter from(@NotNull String topicFilter) {
        checkTopic(topicFilter);
        int multiPos = topicFilter.indexOf(MULTI_LEVEL_WILDCARD);
        if (multiPos != -1 && multiPos != topicFilter.length() - 1) {
            throw new IllegalArgumentException("Multi level wildcard is incorrectly used: " + topicFilter);
        } else if (topicFilter.contains("++")) {
            throw new IllegalArgumentException("Single level wildcard is incorrectly used: " + topicFilter);
        } else if(topicFilter.startsWith("$shared")) {
            int firstSlash = topicFilter.indexOf(DELIMITER);
            int secondSlash = topicFilter.indexOf(DELIMITER, firstSlash);
            var group = topicFilter.substring(firstSlash, secondSlash);
            var realTopicFilter = topicFilter.substring(secondSlash);
            return new SharedTopicFilter(realTopicFilter, group);
        } else {
            return new TopicFilter(topicFilter);
        }
    }

    public static boolean isShared(@NotNull TopicFilter topicFilter) {
        return topicFilter instanceof SharedTopicFilter;
    }

    TopicFilter(@NotNull String topicFilter) {
        super(topicFilter);
    }
}

