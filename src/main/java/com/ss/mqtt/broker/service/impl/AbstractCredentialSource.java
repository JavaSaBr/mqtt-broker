package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.service.CredentialSource;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public abstract class AbstractCredentialSource implements CredentialSource {

    private final LockableRefToRefDictionary<String, byte[]> credentials =
        DictionaryFactory.stampedLockBasedRefToRefDictionary(String.class, byte[].class);

    abstract void init();

    void putAll(@NotNull RefToRefDictionary<String, byte[]> creds) {
      long stamp = credentials.writeLock();
      try {
        credentials.append(creds);
      } finally {
        credentials.writeUnlock(stamp);
      }
    }

    void put(@NotNull String user, @NotNull byte[] pass) {
      long stamp = credentials.writeLock();
      try {
        credentials.put(user, pass);
      } finally {
        credentials.writeUnlock(stamp);
      }
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
