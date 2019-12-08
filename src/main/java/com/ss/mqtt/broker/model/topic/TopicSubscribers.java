package com.ss.mqtt.broker.model.topic;

import static com.ss.mqtt.broker.model.topic.AbstractTopic.MULTI_LEVEL_WILDCARD;
import static com.ss.mqtt.broker.model.topic.AbstractTopic.SINGLE_LEVEL_WILDCARD;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.function.NotNullSupplier;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ConcurrentArray;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TopicSubscribers {

    private final static NotNullSupplier<TopicSubscribers> TOPIC_SUBSCRIBER_SUPPLIER = TopicSubscribers::new;

    private static boolean removeDuplicateWithLowerQoS(
        @NotNull Array<Subscriber> subscribers,
        @NotNull Subscriber candidate
    ) {
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

    private static @Nullable TopicSubscribers collectSubscribers(
        @NotNull ObjectDictionary<String, TopicSubscribers> subscribersMap,
        @NotNull String segment,
        @NotNull Array<Subscriber> result
    ) {

        var topicSubscribers = subscribersMap.get(segment);
        if (topicSubscribers == null) {
            return null;
        }
        var subscribers = topicSubscribers.getSubscribers();
        if (subscribers != null) {
            long stamp = subscribers.readLock();
            try {
                subscribers.forEachFiltered(
                    result,
                    TopicSubscribers::removeDuplicateWithLowerQoS,
                    Array::add
                );
            } finally {
                subscribers.readUnlock(stamp);
            }
        }
        return topicSubscribers;
    }

    private volatile @Getter @Nullable ConcurrentObjectDictionary<String, TopicSubscribers> topicSubscribers;
    private volatile @Getter @Nullable ConcurrentArray<Subscriber> subscribers;

    public void addSubscriber(@NotNull MqttClient client, @NotNull SubscribeTopicFilter subscribe) {
        addSubscriber(0, subscribe.getTopicFilter(), new Subscriber(client, subscribe));
    }

    private void addSubscriber(int level, @NotNull TopicFilter topicFilter, @NotNull Subscriber subscriber) {
        if (level == topicFilter.levelsCount()) {
            getOrCreateSubscribers().runInWriteLock(subscriber, Array::add);
        } else {
            var topicSubscriber = getOrCreateTopicSubscribers().getInWriteLock(
                topicFilter.getSegment(level),
                TOPIC_SUBSCRIBER_SUPPLIER,
                ObjectDictionary::getOrCompute
            );
            //noinspection ConstantConditions
            topicSubscriber.addSubscriber(level + 1, topicFilter, subscriber);
        }
    }

    public boolean removeSubscriber(@NotNull MqttClient client, @NotNull SubscribeTopicFilter subscribe) {
        return removeSubscriber(client, subscribe.getTopicFilter());
    }

    public boolean removeSubscriber(@NotNull MqttClient mqttClient, @NotNull TopicFilter topicFilter) {
        return removeSubscriber(0, topicFilter, mqttClient);
    }

    private boolean removeSubscriber(int level, @NotNull TopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        var topicSubscribers = getTopicSubscribers();
        var subscribers = getSubscribers();
        if (subscribers != null && level == topicFilter.levelsCount()) {
            return subscribers.removeIfConvertedInWriteLock(
                mqttClient,
                Subscriber::getMqttClient,
                Object::equals
            );
        } else if (topicSubscribers != null) {
            var topicSubscriber = topicSubscribers.getInReadLock(
                topicFilter.getSegment(level),
                ObjectDictionary::get
            );
            if (topicSubscriber == null) {
                return false;
            } else {
                return topicSubscriber.removeSubscriber(level + 1, topicFilter, mqttClient);
            }
        } else {
            return false;
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
        @NotNull Array<Subscriber> result
    ) {
        var topicSubscribers = getTopicSubscribers();
        if (topicSubscribers == null) {
            return;
        }
        var topicSubscriber = topicSubscribers.getInReadLock(
            segment,
            result,
            TopicSubscribers::collectSubscribers
        );
        if (topicSubscriber != null && nextLevel < topicName.levelsCount()) {
            var nextSegment = topicName.getSegment(nextLevel);
            topicSubscriber.processLevel(nextLevel, nextSegment, topicName, result);
        }
    }

    private @NotNull ConcurrentObjectDictionary<String, TopicSubscribers> getOrCreateTopicSubscribers() {
        if (topicSubscribers == null) {
            synchronized (this) {
                if (topicSubscribers == null) {
                    topicSubscribers = ConcurrentObjectDictionary.ofType(String.class, TopicSubscribers.class);
                }
            }
        }
        //noinspection ConstantConditions
        return topicSubscribers;
    }

    private @NotNull ConcurrentArray<Subscriber> getOrCreateSubscribers() {
        if (subscribers == null) {
            synchronized (this) {
                if (subscribers == null) {
                    subscribers = ConcurrentArray.ofType(Subscriber.class);
                }
            }
        }
        //noinspection ConstantConditions
        return subscribers;
    }
}
