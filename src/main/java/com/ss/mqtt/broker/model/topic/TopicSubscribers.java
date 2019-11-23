package com.ss.mqtt.broker.model.topic;

import static com.ss.mqtt.broker.model.topic.AbstractTopic.MULTI_LEVEL_WILDCARD;
import static com.ss.mqtt.broker.model.topic.AbstractTopic.SINGLE_LEVEL_WILDCARD;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.function.NotNullSupplier;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ConcurrentArray;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TopicSubscribers {

    private final static NotNullSupplier<TopicSubscribers> TOPIC_SUBSCRIBER_SUPPLIER = TopicSubscribers::new;

    private static boolean removeDuplicateWithLowerQoS(@NotNull Array<Subscriber> subscribers, @NotNull Subscriber candidate) {
        var found = subscribers.indexOf(candidate);
        if (found == -1) {
            return true;
        }
        var existed = subscribers.get(found);
        if (existed.getQos().ordinal() < candidate.getQos().ordinal()) {
            subscribers.fastRemove(found);
            return true;
        } else {
            return false;
        }
    }

    private static @NotNull TopicSubscribers collectSubscribers(
        @NotNull ConcurrentObjectDictionary<String, TopicSubscribers> topicSubscribers,
        @NotNull String topicName,
        @NotNull Array<Subscriber> resultSubscribers
    ) {
        var ts = topicSubscribers.get(topicName);
        if (ts == null) {
            return null;
        }
        var subscribers = ts.getSubscribers();
        long stamp = subscribers.readLock();
        try {
            subscribers.forEachFiltered(
                resultSubscribers,
                TopicSubscribers::removeDuplicateWithLowerQoS,
                Array::add
            );
        } finally {
            subscribers.readUnlock(stamp);
        }
        return ts;
    }

    private @Nullable ConcurrentObjectDictionary<String, TopicSubscribers> topicSubscribers;
    private @Nullable ConcurrentArray<Subscriber> subscribers;

    public void addSubscriber(@NotNull TopicFilter topicFilter, @NotNull Subscriber subscriber) {
        addSubscriber(0, topicFilter, subscriber);
    }

    private void addSubscriber(int level, @NotNull TopicFilter topicFilter, @NotNull Subscriber subscriber) {
        if (level == topicFilter.levelsCount()) {
            getSubscribers().runInWriteLock(subscriber, Array::add);
        } else {
            var topicSubscriber = getTopicSubscribers().getInWriteLock(
                topicFilter.getSegment(level),
                TOPIC_SUBSCRIBER_SUPPLIER,
                ObjectDictionary::getOrCompute
            );
            //noinspection ConstantConditions
            topicSubscriber.addSubscriber(level + 1, topicFilter, subscriber);
        }
    }

    public boolean removeSubscriber(@NotNull TopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        return removeSubscriber(0, topicFilter, mqttClient);
    }

    private boolean removeSubscriber(int level, @NotNull TopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        if (level == topicFilter.levelsCount()) {
            return getSubscribers().removeConvertedIfInWriteLock(
                mqttClient,
                Subscriber::getMqttClient,
                Object::equals
            );
        } else {
            var topicSubscriber = getTopicSubscribers().getInReadLock(
                topicFilter.getSegment(level),
                ObjectDictionary::get
            );
            if (topicSubscriber == null) {
                return false;
            } else {
                return topicSubscriber.removeSubscriber(level + 1, topicFilter, mqttClient);
            }
        }
    }

    public @NotNull Array<Subscriber> matches(@NotNull TopicName topicName) {
        var resultArray = Array.ofType(Subscriber.class);
        processLevel(0, topicName.getSegment(0), topicName, resultArray);
        return resultArray;
    }

    private void processLevel(
        int level,
        @NotNull String segment,
        @NotNull TopicName topicName,
        @NotNull Array<Subscriber> resultSubscribers
    ) {
        var nextLevel = level + 1;
        processSegment(nextLevel, segment, topicName, resultSubscribers);
        processSegment(nextLevel, SINGLE_LEVEL_WILDCARD, topicName, resultSubscribers);
        processSegment(nextLevel, MULTI_LEVEL_WILDCARD, topicName, resultSubscribers);
    }

    private void processSegment(
        int nextLevel,
        @NotNull String segment,
        @NotNull TopicName topicName,
        @NotNull Array<Subscriber> resultSubscribers
    ) {
        var topicSubscriber = getTopicSubscribers().getInReadLock(
            segment,
            resultSubscribers,
            TopicSubscribers::collectSubscribers
        );
        if (topicSubscriber != null && nextLevel < topicName.levelsCount()) {
            var nextSegment = topicName.getSegment(nextLevel);
            topicSubscriber.processLevel(nextLevel, nextSegment, topicName, resultSubscribers);
        }
    }

    private @NotNull ConcurrentObjectDictionary<String, TopicSubscribers> getTopicSubscribers() {
        if (topicSubscribers == null) {
            synchronized (this) {
                if (topicSubscribers == null) {
                    topicSubscribers = ConcurrentObjectDictionary.ofType(String.class, TopicSubscribers.class);
                }
            }
        }
        return topicSubscribers;
    }

    private @NotNull ConcurrentArray<Subscriber> getSubscribers() {
        if (subscribers == null) {
            synchronized (this) {
                if (subscribers == null) {
                    subscribers = ConcurrentArray.ofType(Subscriber.class);
                }
            }
        }
        return subscribers;
    }
}
