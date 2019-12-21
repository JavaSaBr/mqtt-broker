package com.ss.mqtt.broker.model.topic;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class SharedTopicFilter extends TopicFilter {

    private final @Getter @NotNull String group;

    public SharedTopicFilter(@NotNull String topicFilter, @NotNull String group) {
        super(topicFilter);
        this.group = group;
    }
}

