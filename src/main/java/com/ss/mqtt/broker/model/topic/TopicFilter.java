package com.ss.mqtt.broker.model.topic;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class TopicFilter extends AbstractTopic {

    public TopicFilter(@NotNull String topicFilter) {
        super(topicFilter);
    }
}

