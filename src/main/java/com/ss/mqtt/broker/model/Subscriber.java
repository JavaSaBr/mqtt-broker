package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.model.topic.SharedTopicFilter;
import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode(of = "mqttClient")
@RequiredArgsConstructor
public class Subscriber {

    private final @Getter @NotNull MqttClient mqttClient;
    private final @NotNull SubscribeTopicFilter subscribeTopicFilter;

    public @NotNull QoS getQos() {
        return subscribeTopicFilter.getQos();
    }

    public @NotNull TopicFilter getTopicFilter() {
        return subscribeTopicFilter.getTopicFilter();
    }

    public boolean isShared() {
        return TopicFilter.isShared(subscribeTopicFilter.getTopicFilter());
    }

    public @NotNull String getGroup() {
        if (isShared()) {
            return ((SharedTopicFilter) subscribeTopicFilter.getTopicFilter()).getGroup();
        } else {
            return StringUtils.EMPTY;
        }
    }
}
