package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.model.topic.SharedTopicFilter;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.rlib.common.util.array.Array;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.locks.StampedLock;

@RequiredArgsConstructor
public class SharedSubscriber {

    private final @Getter @NotNull String group;
    private final Array<Subscriber> subscribers;
    private final StampedLock lock;
    private int current;

    public SharedSubscriber(@NotNull Subscriber subscriber) {
        group = ((SharedTopicFilter) subscriber.getTopicFilter()).getGroup();
        subscribers = Array.ofType(Subscriber.class);
        subscribers.add(subscriber);
        lock = new StampedLock();
        current = 0;
    }

    public @NotNull Subscriber getSubscriber() {
        long stamp = lock.writeLock();
        try {
            if (current == subscribers.size()) {
                current = 0;
            }
            return subscribers.get(current++);
        } finally {
            lock.unlockWrite(stamp);
        }

    }

    public void addSubscriber(@NotNull Subscriber subscriber) {
        long stamp = lock.writeLock();
        try {
            subscribers.add(subscriber);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public boolean removeSubscriber(@NotNull MqttClient client) {
        long stamp = lock.writeLock();
        try {
            var result = subscribers.removeIfConverted(
                client,
                Subscriber::getMqttClient,
                Objects::equals
            );
            if (result) {
                current--;
            }
            return result;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public int size() {
        long stamp = lock.writeLock();
        try {
            return subscribers.size();
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
