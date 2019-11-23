package com.ss.mqtt.broker.model.topic;

import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.StringUtils;
import com.ss.rlib.common.util.array.Array;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class AbstractTopic {

    static final String DELIMITER = "/";
    static final String MULTI_LEVEL_WILDCARD = "#";
    static final String SINGLE_LEVEL_WILDCARD = "+";

    static void checkTopic(@NotNull String topic) {
        if (topic.length() == 0) {
            throw new IllegalArgumentException("Topic has zero length.");
        } else if (topic.contains("//") || topic.startsWith("/") || topic.endsWith("/")) {
            throw new IllegalArgumentException("Topic has zero length level: " + topic);
        }
    }

    private final int length;
    private final String[] segments;
    private final String rawTopic;

    AbstractTopic() {
        length = 0;
        segments = ArrayUtils.EMPTY_STRING_ARRAY;
        rawTopic = StringUtils.EMPTY;
    }

    AbstractTopic(@NotNull String topicName) {

        length = topicName.length();
        segments = topicName.split(DELIMITER);
        rawTopic = topicName;
    }

    @Override
    public @NotNull String toString() {
        return rawTopic;
    }

    @NotNull String getSegment(int level) {
        return segments[level];
    }

    int levelsCount() {
        return segments.length;
    }
}
