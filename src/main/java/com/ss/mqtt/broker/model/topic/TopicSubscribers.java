package com.ss.mqtt.broker.model.topic;

import static com.ss.mqtt.broker.model.topic.AbstractTopic.MULTI_LEVEL_WILDCARD;
import static com.ss.mqtt.broker.model.topic.AbstractTopic.SINGLE_LEVEL_WILDCARD;
import com.ss.mqtt.broker.model.*;
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

    private static void createSharedSubscriber(
        @NotNull ConcurrentArray<Subscriber> subscribers,
        @NotNull MqttClient client,
        @NotNull SubscribeTopicFilter subscribe
    ) {
        var group = ((SharedTopicFilter) subscribe.getTopicFilter()).getGroup();

        var sharedSubscriber = (SharedSubscriber) subscribers.findAny(
            group,
            TopicSubscribers::sharedSubscriberWithGroup
        );

        if (sharedSubscriber == null) {
            sharedSubscriber = new SharedSubscriber(subscribe);
            sharedSubscriber.addSubscriber(client);
            subscribers.add(sharedSubscriber);
        } else {
            sharedSubscriber.addSubscriber(client);
        }
    }

    private static boolean sharedSubscriberWithGroup(@NotNull String group, @NotNull Subscriber subscriber) {
        return subscriber instanceof SharedSubscriber && group.equals(((SharedSubscriber) subscriber).getGroup());
    }

    private static boolean removeSharedSubscriber(
        @NotNull ConcurrentArray<Subscriber> subscribers,
        @NotNull SharedTopicFilter topic,
        @NotNull MqttClient client
    ) {
        boolean removed = false;
        var sharedSubscriber = (SharedSubscriber) subscribers.findAny(
            topic.getGroup(),
            TopicSubscribers::sharedSubscriberWithGroup
        );

        if (sharedSubscriber != null) {
            removed = sharedSubscriber.removeSubscriber(client);
            if (removed && sharedSubscriber.size() == 0) {
                subscribers.remove(sharedSubscriber);
            }
        }
        return removed;
    }

    private static boolean removeDuplicateWithLowerQoS(
        @NotNull Array<Subscriber> subscribers,
        @NotNull Subscriber candidate
    ) {
        var found = subscribers.indexOf(candidate);
        if (found == -1) {
            return true;
        }
        var existed = (SingleSubscriber) subscribers.get(found);
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
        addSubscriber(0, subscribe.getTopicFilter(), client, subscribe);
    }

    private void addSubscriber(
        int level,
        @NotNull TopicFilter topicFilter,
        @NotNull MqttClient client,
        @NotNull SubscribeTopicFilter subscribe
    ) {
        if (level == topicFilter.levelsCount()) {
            createSubscriber(client, subscribe);
        } else {
            var topicSubscriber = getOrCreateTopicSubscribers().getInWriteLock(
                topicFilter.getSegment(level),
                TOPIC_SUBSCRIBER_SUPPLIER,
                ObjectDictionary::getOrCompute
            );
            //noinspection ConstantConditions
            topicSubscriber.addSubscriber(level + 1, topicFilter, client, subscribe);
        }
    }

    private void createSubscriber(
        @NotNull MqttClient client,
        @NotNull SubscribeTopicFilter subscribe
    ) {
        if (subscribe.getTopicFilter() instanceof SharedTopicFilter) {
            getOrCreateSubscribers().runInReadLock(
                client,
                subscribe,
                TopicSubscribers::createSharedSubscriber
            );
        } else {
            getOrCreateSubscribers().runInWriteLock(
                new SingleSubscriber(client, subscribe),
                Array::add
            );
        }
    }

    public void removeSubscriber(@NotNull MqttClient client, @NotNull SubscribeTopicFilter subscribe) {
        removeSubscriber(client, subscribe.getTopicFilter());
    }

    public boolean removeSubscriber(@NotNull MqttClient client, @NotNull TopicFilter topicFilter) {
        return removeSubscriber(0, topicFilter, client);
    }

    private boolean removeSubscriber(int level, @NotNull TopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        var removed = false;
        var topicSubscribers = getTopicSubscribers();
        if (level == topicFilter.levelsCount()) {
            removed = removeSubscriber(topicFilter, mqttClient);
        } else if (topicSubscribers != null) {
            var topicSubscriber = topicSubscribers.getInReadLock(
                topicFilter.getSegment(level),
                ObjectDictionary::get
            );
            if (topicSubscriber != null) {
                removed = topicSubscriber.removeSubscriber(level + 1, topicFilter, mqttClient);
            }
        }
        return removed;
    }

    private boolean removeSubscriber(@NotNull TopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        var removed = false;
        var subscribers = getSubscribers();
        if (subscribers != null) {
            if (topicFilter instanceof SharedTopicFilter) {
                //noinspection ConstantConditions
                removed = subscribers.getInWriteLock(
                    (SharedTopicFilter) topicFilter,
                    mqttClient,
                    TopicSubscribers::removeSharedSubscriber
                );
            } else {
                removed = subscribers.removeIfConvertedInWriteLock(
                    mqttClient,
                    Subscriber::getMqttClient,
                    Object::equals
                );
            }
        }
        return removed;
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
