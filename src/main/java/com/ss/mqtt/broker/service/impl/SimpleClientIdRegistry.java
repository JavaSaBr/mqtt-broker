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

    private static final String AVAILABLE_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-";
    private static final BitSet AVAILABLE_CHAR_SET;
    private static final Object CLIENT_ID_VALUE = new Object();

    static {

        var bitSet = new BitSet();

        for (char ch : AVAILABLE_CHARS.toCharArray()) {
            bitSet.set(ch, true);
        }

        AVAILABLE_CHAR_SET = bitSet;
    }

    private final @NotNull ConcurrentObjectDictionary<String, Object> clientIdRegistry;

    public SimpleClientIdRegistry() {
        this.clientIdRegistry = DictionaryFactory.newConcurrentStampedLockObjectDictionary();
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
        var value = clientIdRegistry.getInWriteLock(clientId, ObjectDictionary::remove);
        return Mono.just(value != null);
    }

    @Override
    public boolean validate(@NotNull String clientId) {

        for (int i = 0, length = clientId.length(); i < length; i++) {
            if (!AVAILABLE_CHAR_SET.get(clientId.charAt(i))) {
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
