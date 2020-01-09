package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.model.topic.SharedTopicFilter;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ConcurrentArray;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedSubscriber implements Subscriber {

    private final @NotNull SharedTopicFilter topicFilter;
    private final @NotNull ConcurrentArray<SingleSubscriber> subscribers;
    private final @NotNull AtomicInteger current;

    public SharedSubscriber(@NotNull SubscribeTopicFilter topic) {
        subscribers = ConcurrentArray.ofType(Subscriber.class);
        current = new AtomicInteger(0);
        topicFilter = (SharedTopicFilter) topic.getTopicFilter();
    }

    public @NotNull SingleSubscriber getSubscriber() {
        //noinspection ConstantConditions
        return subscribers.getInReadLock(current.getAndIncrement(), SharedSubscriber::next);
    }

    public void addSubscriber(@NotNull SingleSubscriber client) {
        subscribers.runInWriteLock(client, Array::add);
    }

    public boolean removeSubscriber(@NotNull MqttClient client) {
        return subscribers.removeIfConvertedInWriteLock(client, SingleSubscriber::getMqttClient, Objects::equals);
    }

    public int size() {
        //noinspection ConstantConditions
        return subscribers.getInReadLock(Collection::size);
    }

    private static @NotNull SingleSubscriber next(@NotNull Array<SingleSubscriber> subscribers, int current) {
        return subscribers.get(current % subscribers.size());
    }

    public @NotNull String getGroup() {
        return topicFilter.getGroup();
    }
}
