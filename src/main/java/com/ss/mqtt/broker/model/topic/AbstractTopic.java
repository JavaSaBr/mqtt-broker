package com.ss.mqtt.broker.model.topic;

import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString(of = "rawTopic")
@EqualsAndHashCode(of = "rawTopic")
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

    private final @NotNull String[] segments;
    private final @NotNull String rawTopic;

    private final int length;

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

    @NotNull String getSegment(int level) {
        return segments[level];
    }

    int levelsCount() {
        return segments.length;
    }
}
