package com.ss.mqtt.broker.model.topic;

import static com.ss.mqtt.broker.util.TopicUtils.*;
import com.ss.mqtt.broker.model.SharedSubscriber;
import com.ss.mqtt.broker.model.SingleSubscriber;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.util.SubscriberUtils;
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

    private static void addSubscriber(
        @NotNull ConcurrentArray<Subscriber> subscribers,
        @NotNull MqttClient client,
        @NotNull SubscribeTopicFilter subscribe
    ) {
        if (isShared(subscribe.getTopicFilter())) {
            addSharedSubscriber(subscribers, client, subscribe);
        } else {
            addSingleSubscriber(subscribers, client, subscribe);
        }
    }

    private static void addSingleSubscriber(
        @NotNull ConcurrentArray<Subscriber> subscribers,
        @NotNull MqttClient client,
        @NotNull SubscribeTopicFilter subscribe
    ) {
        subscribers.add(new SingleSubscriber(client, subscribe));
    }

    private static void addSharedSubscriber(
        @NotNull ConcurrentArray<Subscriber> subscribers,
        @NotNull MqttClient client,
        @NotNull SubscribeTopicFilter subscribe
    ) {
        var group = ((SharedTopicFilter) subscribe.getTopicFilter()).getGroup();

        var sharedSubscriber = (SharedSubscriber) subscribers.findAny(
            group,
            SubscriberUtils::sharedSubscriberWithGroup
        );

        if (sharedSubscriber == null) {
            sharedSubscriber = new SharedSubscriber(subscribe);
            subscribers.add(sharedSubscriber);
        }

        var singleSubscriber = new SingleSubscriber(client, subscribe);
        sharedSubscriber.addSubscriber(singleSubscriber);
    }

    private static boolean removeSubscriber(
        @NotNull ConcurrentArray<Subscriber> subscribers,
        @NotNull TopicFilter topic,
        @NotNull MqttClient client
    ) {
        return isShared(topic) ?
            removeSharedSubscriber(subscribers, ((SharedTopicFilter) topic).getGroup(), client) :
            removeSingleSubscriber(subscribers, client);
    }

    private static boolean removeSingleSubscriber(
        @NotNull ConcurrentArray<Subscriber> subscribers,
        @NotNull MqttClient client
    ) {
        //noinspection NullableProblems
        return subscribers.removeIfConverted(
            client,
            SubscriberUtils::singleSubscriberToMqttClient,
            Object::equals
        );
    }

    private static boolean removeSharedSubscriber(
        @NotNull ConcurrentArray<Subscriber> subscribers,
        @NotNull String group,
        @NotNull MqttClient client
    ) {
        boolean removed = false;
        var sharedSubscriber = (SharedSubscriber) subscribers.findAny(
            group,
            SubscriberUtils::sharedSubscriberWithGroup
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
        @NotNull Array<SingleSubscriber> result,
        @NotNull Subscriber candidate
    ) {
        if (candidate instanceof SharedSubscriber) {
            return true;
        }
        var found = result.indexOf(candidate);
        if (found == -1) {
            return true;
        }
        var existed = result.get(found);
        if (existed.getQos().ordinal() < ((SingleSubscriber) candidate).getQos().ordinal()) {
            result.fastRemove(found);
            return true;
        } else {
            return false;
        }
    }

    private static void addToResultArray(@NotNull Array<SingleSubscriber> result, @NotNull Subscriber subscriber) {
        if (subscriber instanceof SharedSubscriber) {
            result.add(((SharedSubscriber) subscriber).getSubscriber());
        } else {
            result.add((SingleSubscriber) subscriber);
        }
    }

    private static @Nullable TopicSubscribers collectSubscribers(
        @NotNull ObjectDictionary<String, TopicSubscribers> subscribersMap,
        @NotNull String segment,
        @NotNull Array<SingleSubscriber> result
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
                    TopicSubscribers::addToResultArray
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
        searchPlaceForSubscriber(0, subscribe.getTopicFilter(), client, subscribe);
    }

    private void searchPlaceForSubscriber(
        int level,
        @NotNull TopicFilter topicFilter,
        @NotNull MqttClient client,
        @NotNull SubscribeTopicFilter subscribe
    ) {
        if (level == topicFilter.levelsCount()) {
            getOrCreateSubscribers().runInWriteLock(
                client,
                subscribe,
                TopicSubscribers::addSubscriber
            );
        } else {
            var topicSubscriber = getOrCreateTopicSubscribers().getInWriteLock(
                topicFilter.getSegment(level),
                TOPIC_SUBSCRIBER_SUPPLIER,
                ObjectDictionary::getOrCompute
            );
            //noinspection ConstantConditions
            topicSubscriber.searchPlaceForSubscriber(level + 1, topicFilter, client, subscribe);
        }
    }

    public void removeSubscriber(@NotNull MqttClient client, @NotNull SubscribeTopicFilter subscribe) {
        removeSubscriber(client, subscribe.getTopicFilter());
    }

    public boolean removeSubscriber(@NotNull MqttClient client, @NotNull TopicFilter topicFilter) {
        return searchSubscriberToRemove(0, topicFilter, client);
    }

    private boolean searchSubscriberToRemove(int level, @NotNull TopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        var removed = false;
        var topicSubscribers = getTopicSubscribers();
        if (level == topicFilter.levelsCount()) {
            removed = tryToRemoveSubscriber(topicFilter, mqttClient);
        } else if (topicSubscribers != null) {
            var topicSubscriber = topicSubscribers.getInReadLock(
                topicFilter.getSegment(level),
                ObjectDictionary::get
            );
            if (topicSubscriber != null) {
                removed = topicSubscriber.searchSubscriberToRemove(level + 1, topicFilter, mqttClient);
            }
        }
        return removed;
    }

    private boolean tryToRemoveSubscriber(@NotNull TopicFilter topicFilter, @NotNull MqttClient mqttClient) {
        var removed = false;
        var subscribers = getSubscribers();
        if (subscribers != null) {
            //noinspection ConstantConditions
            removed = subscribers.getInWriteLock(
                topicFilter,
                mqttClient,
                TopicSubscribers::removeSubscriber
            );
        }
        return removed;
    }

    public @NotNull Array<SingleSubscriber> matches(@NotNull TopicName topicName) {
        var resultArray = Array.ofType(SingleSubscriber.class);
        processLevel(0, topicName.getSegment(0), topicName, resultArray);
        return resultArray;
    }

    private void processLevel(
        int level,
        @NotNull String segment,
        @NotNull TopicName topicName,
        @NotNull Array<SingleSubscriber> result
    ) {
        var nextLevel = level + 1;
        processSegment(nextLevel, segment, topicName, result);
        processSegment(nextLevel, SINGLE_LEVEL_WILDCARD, topicName, result);
        processSegment(nextLevel, MULTI_LEVEL_WILDCARD, topicName, result);
    }

    private void processSegment(
        int nextLevel,
        @NotNull String segment,
        @NotNull TopicName topicName,
        @NotNull Array<SingleSubscriber> result
    ) {
        var subscribersMap = getTopicSubscribers();
        if (subscribersMap == null) {
            return;
        }
        var topicSubscribers = subscribersMap.getInReadLock(
            segment,
            result,
            TopicSubscribers::collectSubscribers
        );
        if (topicSubscribers != null && nextLevel < topicName.levelsCount()) {
            var nextSegment = topicName.getSegment(nextLevel);
            topicSubscribers.processLevel(nextLevel, nextSegment, topicName, result);
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
