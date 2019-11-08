package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.BitSet;
import java.util.UUID;

public class InMemoryClientIdRegistry implements ClientIdRegistry {

    private static final Object CLIENT_ID_VALUE = new Object();

    private final @NotNull ConcurrentObjectDictionary<String, Object> clientIdRegistry;
    private final @NotNull BitSet availableCharSet;

    private final int maxClientIdLength;

    public InMemoryClientIdRegistry(@NotNull String availableChars, int maxClientIdLength) {
        this.maxClientIdLength = maxClientIdLength;
        this.clientIdRegistry = ConcurrentObjectDictionary.ofType(String.class, Object.class);
        this.availableCharSet = new BitSet();

        for (char ch : availableChars.toCharArray()) {
            availableCharSet.set(ch, true);
        }
    }

    @Override
    public @NotNull Mono<Boolean> register(@NotNull String clientId) {

        if (!validate(clientId)) {
            return Mono.just(false);
        }

        var result = clientIdRegistry.getInWriteLock(clientId, (dictionary, id) -> {

            if (dictionary.containsKey(id)) {
                return false;
            } else {
                dictionary.put(id, CLIENT_ID_VALUE);
            }

            return true;
        });

        //noinspection ConstantConditions
        return Mono.just(result);
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
        while (true) {

            var clientId = UUID.randomUUID().toString();
            var contains = clientIdRegistry.getInReadLock(clientId, ObjectDictionary::containsKey);

            if (contains == Boolean.FALSE) {
                return Mono.just(clientId);
            }
        }
    }
}
