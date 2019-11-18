package com.ss.mqtt.broker.model;

import static com.ss.mqtt.broker.model.AbstractTopic.MULTI_LEVEL_WILDCARD;
import static com.ss.mqtt.broker.model.AbstractTopic.SINGLE_LEVEL_WILDCARD;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ConcurrentArray;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;

public class TopicSubscriber {

    private static boolean filterByQos(@NotNull Array<Subscriber> subscribers, @NotNull Subscriber candidate) {
        var existed = subscribers.findAny(candidate, Subscriber::equals);
        if (existed == null) {
            return true;
        }
        if (existed.getQos().ordinal() < candidate.getQos().ordinal()) {
            return subscribers.remove(existed);
        } else {
            return false;
        }
    }

    private static TopicSubscriber collectSubscribers(
        @NotNull ConcurrentObjectDictionary<String, TopicSubscriber> topicSubscribers,
        @NotNull String topicName,
        @NotNull Array<Subscriber> resultSubscribers
    ) {
        var ts = topicSubscribers.get(topicName);
        if (ts != null) {
            long stamp = ts.subscribers.readLock();
            try {
                ts.subscribers.forEachFiltered(resultSubscribers, TopicSubscriber::filterByQos, Array::add);
            } finally {
                ts.subscribers.readUnlock(stamp);
            }
        }
        return ts;
    }

    private final ConcurrentObjectDictionary<String, TopicSubscriber> topicSubscribers =
        DictionaryFactory.newConcurrentStampedLockObjectDictionary();
    private final ConcurrentArray<Subscriber> subscribers = ConcurrentArray.ofType(Subscriber.class);

    public void addSubscriber(@NotNull TopicFilter topicFilter, @NotNull Subscriber subscriber) {
        addSubscriber(0, topicFilter, subscriber);
    }

    private void addSubscriber(int level, @NotNull TopicFilter topicFilter, @NotNull Subscriber subscriber) {
        if (level == topicFilter.levels.length) {
            subscribers.runInWriteLock(subscriber, ConcurrentArray::add);
        } else {
            var topicSubscriber = topicSubscribers.getInWriteLock(
                topicFilter.levels[level],
                (ts, topic) -> ts.getOrCompute(topic, TopicSubscriber::new)
            );
            //noinspection ConstantConditions
            topicSubscriber.addSubscriber(level + 1, topicFilter, subscriber);
        }
    }

    public boolean removeSubscriber(@NotNull TopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        return removeSubscriber(0, topicFilter, mqttClient);
    }

    private boolean removeSubscriber(int level, @NotNull TopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        if (level == topicFilter.levels.length) {
            return subscribers.removeIfInWriteLock(mqttClient, (client, subscriber) -> client.equals(subscriber.getMqttClient()));
        } else {
            var topicSubscriber = topicSubscribers.getInReadLock(topicFilter.levels[level], ObjectDictionary::get);
            if (topicSubscriber == null) {
                return false;
            } else {
                return topicSubscriber.removeSubscriber(level + 1, topicFilter, mqttClient);
            }
        }
    }

    public @NotNull Array<Subscriber> matches(@NotNull TopicName topicName) {
        var resultArray = Array.ofType(Subscriber.class);
        processLevel(0, topicName.levels[0], topicName, resultArray);
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
        var topicSubscriber = topicSubscribers.getInReadLock(
            segment,
            resultSubscribers,
            TopicSubscriber::collectSubscribers
        );
        if (topicSubscriber != null && nextLevel < topicName.levels.length) {
            var nextSegment = topicName.levels[nextLevel];
            topicSubscriber.processLevel(nextLevel, nextSegment, topicName, resultSubscribers);
        }
    }
}
