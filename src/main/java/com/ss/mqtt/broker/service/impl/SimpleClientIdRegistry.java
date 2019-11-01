package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleClientIdRegistry implements ClientIdRegistry {

    private static final String AVAILABLE_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
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
    public @NotNull CompletableFuture<Boolean> register(@NotNull String clientId) {

        var value = clientIdRegistry.getInReadLock(clientId, ObjectDictionary::get);

        if (value != null) {
            return CompletableFuture.completedFuture(Boolean.FALSE);
        }

        var stamp = clientIdRegistry.writeLock();
        try {

            value = clientIdRegistry.get(clientId);

            if (value != null) {
                return CompletableFuture.completedFuture(Boolean.FALSE);
            }

            clientIdRegistry.put(clientId, CLIENT_ID_VALUE);

        } finally {
            clientIdRegistry.writeUnlock(stamp);
        }

        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> unregister(@NotNull String clientId) {
        var value = clientIdRegistry.getInWriteLock(clientId, ObjectDictionary::remove);
        return CompletableFuture.completedFuture(value != null);
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
    public @NotNull CompletableFuture<String> generate() {

        var threadId = Thread.currentThread().getId();
        var random = ThreadLocalRandom.current();

        long stamp = clientIdRegistry.readLock();
        try {

            while (true) {

                var clientId = threadId + "-" + random.nextInt(Short.MAX_VALUE) + "-" + System.currentTimeMillis();
                var value = clientIdRegistry.get(clientId);

                if (value == null) {
                    return CompletableFuture.completedFuture(clientId);
                }
            }

        } finally {
            clientIdRegistry.readUnlock(stamp);
        }
    }
}
