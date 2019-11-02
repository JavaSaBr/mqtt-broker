package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.BitSet;
import java.util.UUID;

public class SimpleClientIdRegistry implements ClientIdRegistry {

    private static final Object CLIENT_ID_VALUE = new Object();

    private final @NotNull ConcurrentObjectDictionary<String, Object> clientIdRegistry;
    private final @NotNull BitSet availableCharSet;

    private final int maxClientIdLength;

    public SimpleClientIdRegistry(@NotNull String availableChars, int maxClientIdLength) {
        this.maxClientIdLength = maxClientIdLength;
        this.clientIdRegistry = DictionaryFactory.newConcurrentStampedLockObjectDictionary();
        this.availableCharSet = new BitSet();

        for (char ch : availableChars.toCharArray()) {
            availableCharSet.set(ch, true);
        }
    }

    @Override
    public @NotNull Mono<Boolean> register(@NotNull String clientId) {

        var value = clientIdRegistry.getInReadLock(clientId, ObjectDictionary::get);

        if (value != null) {
            return Mono.just(Boolean.FALSE);
        }

        var stamp = clientIdRegistry.writeLock();
        try {

            value = clientIdRegistry.get(clientId);

            if (value != null) {
                return Mono.just(Boolean.FALSE);
            }

            clientIdRegistry.put(clientId, CLIENT_ID_VALUE);

        } finally {
            clientIdRegistry.writeUnlock(stamp);
        }

        return Mono.just(Boolean.TRUE);
    }

    @Override
    public @NotNull Mono<Boolean> unregister(@NotNull String clientId) {
        return Mono.just(clientIdRegistry.getInWriteLock(clientId, ObjectDictionary::remove) != null);
    }

    @Override
    public boolean validate(@NotNull String clientId) {

        if (clientId.length() > maxClientIdLength) {
            return false;
        }

        for (int i = 0, length = clientId.length(); i < length; i++) {
            if (!availableCharSet.get(clientId.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @NotNull Mono<String> generate() {
        long stamp = clientIdRegistry.readLock();
        try {

            while (true) {

                var clientId = UUID.randomUUID().toString();
                var value = clientIdRegistry.get(clientId);

                if (value == null) {
                    return Mono.just(clientId);
                }
            }

        } finally {
            clientIdRegistry.readUnlock(stamp);
        }
    }
}
