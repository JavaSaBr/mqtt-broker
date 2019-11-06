package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.impl.DefaultMqttSession;
import com.ss.mqtt.broker.service.MqttSessionService;
import com.ss.rlib.common.concurrent.util.ThreadUtils;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.io.Closeable;

public class InMemoryMqttSessionService implements MqttSessionService, Closeable {

    private final @NotNull ConcurrentObjectDictionary<String, MqttSession> storedSession;
    private final @NotNull Thread cleanThread;

    private final int cleanInterval;

    private volatile boolean closed;

    public InMemoryMqttSessionService(int cleanInterval) {
        this.cleanInterval = cleanInterval;
        this.storedSession = DictionaryFactory.newConcurrentStampedLockObjectDictionary();
        this.cleanThread = new Thread(this::cleanup, "InMemoryMqttSessionService-Cleanup");
        this.cleanThread.setPriority(Thread.MIN_PRIORITY);
        this.cleanThread.setDaemon(true);
        this.cleanThread.start();
    }

    private void cleanup() {

        var toCheck = ArrayFactory.newArray(MqttSession.class);
        var toRemove = ArrayFactory.newArray(MqttSession.class);

        while (true) {

            toCheck.clear();
            toRemove.clear();

            ThreadUtils.sleep(cleanInterval);

            if (closed) {
                return;
            }

            var stamp = storedSession.readLock();
            try {
                storedSession.values(toCheck);
            } finally {
                storedSession.readUnlock(stamp);
            }

            var currentTime = System.currentTimeMillis();

            for (var session : toCheck) {
                if (session.getExpirationTime() > currentTime) {
                    toRemove.add(session);
                }
            }

            if (toRemove.isEmpty()) {
                continue;
            }

            stamp = storedSession.writeLock();
            try {
                for (var session : toRemove) {
                    if (session.getExpirationTime() > currentTime) {
                        storedSession.remove(session.getClientId());
                    }
                }
            } finally {
                storedSession.writeUnlock(stamp);
            }
        }
    }

    @Override
    public @NotNull Mono<MqttSession> getOrCreate(@NotNull String clientId) {

        var session = storedSession.getInWriteLock(clientId, ObjectDictionary::remove);

        if (session != null) {
            return Mono.just(session);
        }

        return Mono.just(new DefaultMqttSession());
    }

    @Override
    public @NotNull Mono<MqttSession> createNew(@NotNull String clientId) {
        return Mono.just(new DefaultMqttSession());
    }

    @Override
    public @NotNull Mono<Boolean> store(@NotNull String clientId, @NotNull MqttSession session) {
        storedSession.runInWriteLock(clientId, session, ObjectDictionary::put);
        return Mono.just(Boolean.TRUE);
    }

    @Override
    public void close() {
        closed = true;
        cleanThread.interrupt();
    }
}
