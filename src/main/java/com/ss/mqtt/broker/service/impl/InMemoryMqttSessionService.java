package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.MqttSession.UnsafeMqttSession;
import com.ss.mqtt.broker.model.impl.DefaultMqttSession;
import com.ss.mqtt.broker.service.MqttSessionService;
import com.ss.rlib.common.concurrent.util.ThreadUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.io.Closeable;

@Log4j2
public class InMemoryMqttSessionService implements MqttSessionService, Closeable {

    private final @NotNull ConcurrentObjectDictionary<String, UnsafeMqttSession> storedSession;
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

    @Override
    public @NotNull Mono<MqttSession> restore(@NotNull String clientId) {

        var session = storedSession.getInWriteLock(clientId, ObjectDictionary::remove);

        if (session != null) {
            session.onRestored();
            log.debug("Restored session for client {}", clientId);
        } else {
            log.debug("No stored session for client {}", clientId);
        }

        return Mono.justOrEmpty(session);
    }

    @Override
    public @NotNull Mono<MqttSession> create(@NotNull String clientId) {

        var session = storedSession.getInWriteLock(clientId, ObjectDictionary::remove);

        if (session != null) {
            log.debug("Removed old session for client {}", clientId);
        }

        log.debug("Created new session for client {}", clientId);

        return Mono.just(new DefaultMqttSession(clientId));
    }

    @Override
    public @NotNull Mono<Boolean> store(
        @NotNull String clientId,
        @NotNull MqttSession session,
        long expiryInterval
    ) {

        var unsafe = (UnsafeMqttSession) session;
        unsafe.setExpirationTime(System.currentTimeMillis() + (expiryInterval * 1000));
        unsafe.onPersisted();

        storedSession.runInWriteLock(clientId, unsafe, ObjectDictionary::put);

        log.debug("Stored session for client {}", clientId);

        return Mono.just(Boolean.TRUE);
    }

    private void cleanup() {

        var toCheck = ArrayFactory.newArray(UnsafeMqttSession.class);
        var toRemove = ArrayFactory.newArray(UnsafeMqttSession.class);

        while (!closed) {

            toCheck.clear();
            toRemove.clear();

            ThreadUtils.sleep(cleanInterval);

            storedSession.runInReadLock(toCheck, ObjectDictionary::values);

            if (findToRemove(toCheck, toRemove)) {
                continue;
            }

            storedSession.runInWriteLock(toRemove, (dictionary, array) -> {

                var time = System.currentTimeMillis();

                for (var session : array) {

                    if (session.getExpirationTime() <= time) {
                        continue;
                    }

                    var removed = dictionary.remove(session.getClientId());

                    log.debug("Removed expired session for client {}", session.getClientId());

                    // if we already have new session under the same client id
                    if (removed != null && removed != session) {
                        dictionary.put(session.getClientId(), removed);
                    } else if (removed != null) {
                        removed.clear();
                    }
                }
            });
        }
    }

    private boolean findToRemove(
        @NotNull Array<UnsafeMqttSession> toCheck,
        @NotNull Array<UnsafeMqttSession> toRemove
    ) {

        var currentTime = System.currentTimeMillis();

        for (var session : toCheck) {
            if (session.getExpirationTime() > currentTime) {
                toRemove.add(session);
            }
        }

        return toRemove.isEmpty();
    }

    @Override
    public void close() {
        closed = true;
        cleanThread.interrupt();
    }
}
