package com.ss.mqtt.broker.model.topic;

import static com.ss.mqtt.broker.util.TopicUtils.splitTopic;
import static com.ss.rlib.common.util.StringUtils.EMPTY;
import com.ss.mqtt.broker.util.DebugUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@EqualsAndHashCode(of = "rawTopic")
public abstract class AbstractTopic {

    static {
        DebugUtils.registerIncludedFields("rawTopic");
    }

    private final @NotNull List<String> segments;
    private final @NotNull String rawTopic;
    private final int length;

    AbstractTopic() {
        length = 0;
        segments = List.of();
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

    @Override
    public @NotNull String toString() {
        return rawTopic;
    }
}
