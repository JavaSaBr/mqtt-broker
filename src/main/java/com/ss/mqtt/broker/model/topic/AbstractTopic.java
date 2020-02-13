package com.ss.mqtt.broker.model.topic;

import static com.ss.mqtt.broker.util.TopicUtils.splitTopic;
import static com.ss.rlib.common.util.StringUtils.EMPTY;
import com.ss.mqtt.broker.util.DebugUtils;
import com.ss.rlib.common.util.array.Array;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode(of = "rawTopic")
public abstract class AbstractTopic {

    static {
        DebugUtils.registerIncludedFields("rawTopic");
    }

    private final @NotNull Array<String> segments;
    private final @NotNull String rawTopic;
    private final int length;

    AbstractTopic() {
        length = 0;
        segments = empty();
        rawTopic = EMPTY;
    }

    AbstractTopic(@NotNull String topicName) {
        length = topicName.length();
        segments = splitTopic(topicName);
        rawTopic = topicName;
    }

    @NotNull String getSegment(int level) {
        return segments.get(level);
    }

    int levelsCount() {
        return segments.size();
    }

    String lastSegment() {
        return segments.get(segments.size() - 1);
    }

    @Override
    public @NotNull String toString() {
        return rawTopic;
    }
}
