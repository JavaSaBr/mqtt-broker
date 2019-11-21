package com.ss.mqtt.broker.model.topic;

import com.ss.rlib.common.util.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
abstract class AbstractTopic {

    static final String DELIMITER = "/";
    static final String MULTI_LEVEL_WILDCARD = "#";
    static final String SINGLE_LEVEL_WILDCARD = "+";

    private final int length;
    private final String[] levels;
    private final String string;

    AbstractTopic() {
        length = 0;
        levels = new String[0];
        string = StringUtils.EMPTY;
    }

    AbstractTopic(@NotNull String topicName) {

        length = topicName.length();
        if (length == 0) {
            throw new IllegalArgumentException("Topic name has zero length.");
        }
        if (topicName.contains("//") || topicName.startsWith("/") || topicName.endsWith("/")) {
            throw new IllegalArgumentException("Topic name has zero length level: " + topicName);
        }

        levels = topicName.split(DELIMITER);
        string = topicName;
    }

    @Override
    public String toString() {
        return string;
    }

    String getSegment(int level) {
        return levels[level];
    }

    int size() {
        return levels.length;
    }

    int lastLevel() {
        return levels.length - 1;
    }

}
