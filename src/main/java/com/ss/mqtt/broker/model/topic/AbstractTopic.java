package com.ss.mqtt.broker.model.topic;

import com.ss.mqtt.broker.util.DebugUtils;
import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode(of = "rawTopic")
public abstract class AbstractTopic {

    static {
        DebugUtils.registerIncludedFields("rawTopic");
    }

    static final String DELIMITER = "/";
    static final String MULTI_LEVEL_WILDCARD = "#";
    static final String SINGLE_LEVEL_WILDCARD = "+";

    static boolean checkTopic(@NotNull String topic) {
        return topic.length() != 0 && !topic.contains("//") && !topic.startsWith("/") && !topic.endsWith("/");
    }

    private final @NotNull String[] segments;
    private final @NotNull String rawTopic;
    private final @Getter boolean invalid;

    private final int length;

    AbstractTopic(boolean invalid) {
        length = 0;
        segments = ArrayUtils.EMPTY_STRING_ARRAY;
        rawTopic = StringUtils.EMPTY;
        this.invalid = invalid;
    }

    AbstractTopic(@NotNull String topicName) {
        length = topicName.length();
        segments = topicName.split(DELIMITER);
        rawTopic = topicName;
        invalid = false;
    }

    @NotNull String getSegment(int level) {
        return segments[level];
    }

    int levelsCount() {
        return segments.length;
    }

    @Override
    public @NotNull String toString() {
        return rawTopic;
    }
}
