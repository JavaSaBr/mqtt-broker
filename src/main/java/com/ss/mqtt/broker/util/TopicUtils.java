package com.ss.mqtt.broker.util;

import com.ss.mqtt.broker.model.topic.SharedTopicFilter;
import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.model.topic.TopicName;
import org.jetbrains.annotations.NotNull;

public class TopicUtils {

    private static final TopicFilter INVALID_TOPIC_FILTER = new TopicFilter();
    private static final TopicName INVALID_TOPIC_NAME = new TopicName();
    public static final TopicName EMPTY_TOPIC_NAME = new TopicName();

    private static final String SHARE_KEYWORD = "$share";
    public static final String DELIMITER = "/";
    public static final String MULTI_LEVEL_WILDCARD = "#";
    public static final String SINGLE_LEVEL_WILDCARD = "+";

    public static boolean isInvalid(@NotNull TopicFilter topicFilter) {
        return topicFilter == INVALID_TOPIC_FILTER;
    }

    public static boolean isInvalid(@NotNull TopicName topicName) {
        return topicName == INVALID_TOPIC_NAME;
    }

    public static boolean isShared(@NotNull TopicFilter topicFilter) {
        return topicFilter instanceof SharedTopicFilter;
    }

    public static boolean hasWildcard(@NotNull TopicFilter topicFilter) {
        var topic = topicFilter.getRawTopic();
        return topic.contains(SINGLE_LEVEL_WILDCARD) || topic.contains(MULTI_LEVEL_WILDCARD);
    }

    public static @NotNull TopicName newTopicName(@NotNull String topicName) {
        if (isInvalidTopicName(topicName)) {
            return INVALID_TOPIC_NAME;
        } else {
            return new TopicName(topicName);
        }
    }

    public static @NotNull TopicFilter newTopicFilter(@NotNull String topicFilter) {
        if (isInvalidTopicFilter(topicFilter)) {
            return INVALID_TOPIC_FILTER;
        } else if (isShared(topicFilter)) {
            return newSharedTopicFilter(topicFilter);
        } else {
            return new TopicFilter(topicFilter);
        }
    }

    private static @NotNull TopicFilter newSharedTopicFilter(@NotNull String topicFilter) {
        int firstSlash = topicFilter.indexOf(DELIMITER) + 1;
        int secondSlash = topicFilter.indexOf(DELIMITER, firstSlash);
        var group = topicFilter.substring(firstSlash, secondSlash);
        if (group.length() == 0) {
            return INVALID_TOPIC_FILTER;
        }
        var realTopicFilter = topicFilter.substring(secondSlash + 1);
        return new SharedTopicFilter(realTopicFilter, group);
    }

    private static boolean isInvalidTopicName(@NotNull String topic) {
        return invalid(topic) || topic.contains(MULTI_LEVEL_WILDCARD) || topic.contains(SINGLE_LEVEL_WILDCARD);
    }

    private static boolean isInvalidTopicFilter(@NotNull String topic) {
        if (TopicUtils.invalid(topic) || topic.contains("++")) {
            return true;
        }
        int multiPos = topic.indexOf(MULTI_LEVEL_WILDCARD);
        return multiPos != -1 && multiPos != topic.length() - 1;
    }

    private static boolean invalid(@NotNull String topic) {
        return topic.length() == 0 || topic.contains("//") || topic.startsWith("/") || topic.endsWith("/");
    }

    private static boolean isShared(@NotNull String topicFilter) {
        return topicFilter.startsWith(SHARE_KEYWORD);
    }
}
