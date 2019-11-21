package com.ss.mqtt.broker.model.topic;

import org.jetbrains.annotations.NotNull;

public class TopicFilter extends AbstractTopic {

    public TopicFilter(@NotNull String topicName) {
        super(topicName);

        int multiPos = topicName.indexOf(MULTI_LEVEL_WILDCARD);
        if (multiPos != -1 && multiPos != topicName.length() - 1) {
            throw new IllegalArgumentException("Multi level wildcard is incorrectly used: " + topicName);
        }
        if (topicName.contains("++")) {
            throw new IllegalArgumentException("Single level wildcard is incorrectly used: " + topicName);
        }
    }

    public boolean matches(@NotNull TopicName topicName) {

        if ((size() < topicName.size() && !getSegment(lastLevel()).equals(MULTI_LEVEL_WILDCARD)) ||
            (topicName.size() < size() && !topicName.getSegment(lastLevel()).equals(MULTI_LEVEL_WILDCARD))) {
            return false;
        }

        int maxLength = Math.min(size(), topicName.size());
        for (int level = 0; level < maxLength; level++) {
            if (!getSegment(level).equals(topicName.getSegment(level)) &&
                !getSegment(level).equals(MULTI_LEVEL_WILDCARD) &&
                !topicName.getSegment(level).equals(MULTI_LEVEL_WILDCARD) &&
                !(getSegment(level).equals(SINGLE_LEVEL_WILDCARD) ||
                    topicName.getSegment(level).equals(SINGLE_LEVEL_WILDCARD))) {
                return false;
            }
        }
        return true;
    }
}

