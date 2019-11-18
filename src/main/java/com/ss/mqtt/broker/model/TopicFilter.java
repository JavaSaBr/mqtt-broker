package com.ss.mqtt.broker.model;

import org.jetbrains.annotations.NotNull;

public class TopicFilter extends AbstractTopic {

    public TopicFilter(@NotNull String topicName) {
        super(topicName);

        int multiPos = topicName.indexOf(MULTI_LEVEL_WILDCARD);
        if (multiPos != -1 && multiPos != length - 1) {
            throw new IllegalArgumentException("Multi level wildcard is incorrectly used: " + topicName);
        }
        if (topicName.contains("++")) {
            throw new IllegalArgumentException("Single level wildcard is incorrectly used: " + topicName);
        }
    }

    public boolean matches(@NotNull TopicName topicName) {

        if ((this.levels.length < topicName.levels.length &&
            !this.levels[this.levels.length - 1].equals(MULTI_LEVEL_WILDCARD)) ||
            (topicName.levels.length < this.levels.length &&
                !topicName.levels[topicName.levels.length - 1].equals(MULTI_LEVEL_WILDCARD))) {
            return false;
        }

        int maxLength = Math.min(this.levels.length, topicName.levels.length);
        for (int i = 0; i < maxLength; i++) {
            if (!this.levels[i].equals(topicName.levels[i]) && !this.levels[i].equals(MULTI_LEVEL_WILDCARD) &&
                !topicName.levels[i].equals(MULTI_LEVEL_WILDCARD) && !(this.levels[i].equals(SINGLE_LEVEL_WILDCARD) ||
                topicName.levels[i].equals(SINGLE_LEVEL_WILDCARD))) {
                return false;
            }
        }
        return true;
    }
}

