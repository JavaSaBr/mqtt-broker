package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.service.CredentialSource;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.Dictionary;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public abstract class AbstractCredentialSource implements CredentialSource {

    private final ConcurrentObjectDictionary<String, byte[]> credentials =
        ConcurrentObjectDictionary.ofType(String.class, byte[].class);

    abstract void init();

    void putAll(@NotNull Dictionary<String, byte[]> creds) {
        credentials.runInWriteLock(creds, Dictionary::put);
    }

    void put(@NotNull String user, @NotNull byte[] pass) {
        credentials.runInWriteLock(user, pass, ObjectDictionary::put);
    }

    @Override
    public @NotNull Mono<Boolean> check(@NotNull String user, @NotNull byte[] pass) {
        return Mono.just(Arrays.equals(pass, credentials.get(user)));
    }

    @Override
    public @NotNull Mono<Boolean> check(@NotNull byte[] pass) {
        return Mono.just(Boolean.FALSE);
    }
}
