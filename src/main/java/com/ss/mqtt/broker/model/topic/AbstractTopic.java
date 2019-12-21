package com.ss.mqtt.broker.model.topic;

import static com.ss.mqtt.broker.util.TopicUtils.DELIMITER;
import static com.ss.rlib.common.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static com.ss.rlib.common.util.StringUtils.EMPTY;
import com.ss.mqtt.broker.util.DebugUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode(of = "rawTopic")
public abstract class AbstractTopic {

    static {
        DebugUtils.registerIncludedFields("rawTopic");
    }

    private final @NotNull String[] segments;
    private final @NotNull String rawTopic;
    private final int length;

    AbstractTopic() {
        length = 0;
        segments = EMPTY_STRING_ARRAY;
        rawTopic = EMPTY;
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

    @Override
    public @NotNull String toString() {
        return rawTopic;
    }
}
