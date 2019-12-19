package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.model.topic.SharedTopicFilter;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ConcurrentArray;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedSubscriber extends AbstractSubscriber {

    private final ConcurrentArray<MqttClient> clients;
    private final AtomicInteger current;

    public SharedSubscriber(@NotNull SubscribeTopicFilter topic) {
        super(topic);
        clients = ConcurrentArray.ofType(MqttClient.class);
        current = new AtomicInteger(0);
    }

    @Override
    public @NotNull MqttClient getMqttClient() {
        //noinspection ConstantConditions
        return clients.getInReadLock(current.getAndIncrement(), SharedSubscriber::next);
    }

    public void addSubscriber(@NotNull MqttClient client) {
        clients.runInWriteLock(client, Array::add);
    }

    public boolean removeSubscriber(@NotNull MqttClient client) {
        return clients.removeIfInWriteLock(client, Objects::equals);
    }

    public int size() {
        //noinspection ConstantConditions
        return clients.getInReadLock(Collection::size);
    }

    private static @NotNull MqttClient next(@NotNull Array<MqttClient> subscribers, int current) {
        return subscribers.get(current % subscribers.size());
    }

    public @NotNull String getGroup() {
        return ((SharedTopicFilter) subscribeTopicFilter.getTopicFilter()).getGroup();
    }
}
