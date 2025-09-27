package com.ss.mqtt.broker.model.topic;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class TopicName extends AbstractTopic {

    public TopicName(String topicName) {
        super(topicName);
    }
}

