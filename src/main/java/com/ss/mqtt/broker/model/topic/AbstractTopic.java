package com.ss.mqtt.broker.model.topic;

import com.ss.mqtt.broker.util.DebugUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.ss.mqtt.broker.util.TopicUtils.splitTopic;
import static com.ss.rlib.common.util.StringUtils.EMPTY;

@Getter
@EqualsAndHashCode(of = "rawTopic")
public abstract class AbstractTopic {

    static {
        DebugUtils.registerIncludedFields("rawTopic");
    }

    private static final String[] EMPTY_ARRAY = new String[0];
    private final @NotNull String[] segments;
    private final @NotNull String rawTopic;
    private final int length;

    AbstractTopic() {
        length = 0;

        segments = EMPTY_ARRAY;
        rawTopic = EMPTY;
    }

    AbstractTopic(@NotNull String topicName) {
        length = topicName.length();
        segments = splitTopic(topicName);
        rawTopic = topicName;
    }

    @NotNull String getSegment(int level) {
        return segments[level];
    }

    int levelsCount() {
        return segments.length;
    }

    String lastSegment() {
        return segments[segments.length - 1];
    }

    @Override
    public @NotNull String toString() {
        return rawTopic;
    }
}
