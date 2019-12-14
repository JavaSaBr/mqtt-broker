package com.ss.mqtt.broker.model;

import lombok.EqualsAndHashCode;
import com.ss.mqtt.broker.model.topic.TopicFilter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class SubscribeTopicFilter {

    /**
     * The subscriber's topic filter.
     */
    private final @NotNull TopicFilter topicFilter;

    /**
     * Maximum QoS field. This gives the maximum QoS level at which the Server
     * can send Application Messages to the Client.
     */
    private final @NotNull QoS qos;

    /**
     * This option specifies whether retained messages are sent when the subscription is established.
     * This does not affect the sending of retained messages at any point after the subscribe.
     * If there are no retained messages matching the Topic Filter, all of these values act the same.
     */
    private final @NotNull SubscribeRetainHandling retainHandling;

    /**
     * If the value is true, Application Messages MUST NOT be forwarded to a connection with a ClientID equal
     * to the ClientID of the publishing connection.
     */
    private final boolean noLocal;

    /**
     * If true, Application Messages forwarded using this subscription keep the RETAIN flag they were published with.
     * If false, Application Messages forwarded using this subscription have the RETAIN flag set to 0.
     * Retained messages sent when the subscription is established have the RETAIN flag set to 1.
     */
    private final boolean retainAsPublished;

    public SubscribeTopicFilter(@NotNull String topicFilter, @NotNull QoS qos) {
        this(TopicFilter.from(topicFilter), qos, SubscribeRetainHandling.SEND, true, true);
    }

    public SubscribeTopicFilter(@NotNull TopicFilter topicFilter, @NotNull QoS qos) {
        this(topicFilter, qos, SubscribeRetainHandling.SEND, true, true);
    }

    @Override
    public @NotNull String toString() {
        return "SubscribeTopicFilter(" + "topicFilter=" + topicFilter.getRawTopic() + ", qos=" + qos + ", retainHandling=" +
            retainHandling + ", noLocal=" + noLocal + ", retainAsPublished=" + retainAsPublished + ')';
    }
}
