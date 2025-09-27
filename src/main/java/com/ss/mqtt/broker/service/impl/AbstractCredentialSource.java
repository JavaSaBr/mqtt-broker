package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.service.CredentialSource;
import java.util.Arrays;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import reactor.core.publisher.Mono;

public abstract class AbstractCredentialSource implements CredentialSource {

  private final LockableRefToRefDictionary<String, byte[]> credentials =
      DictionaryFactory.stampedLockBasedRefToRefDictionary(String.class, byte[].class);

  abstract void init();

  void putAll(RefToRefDictionary<String, byte[]> otherCredentials) {
    long stamp = credentials.writeLock();
    try {
      credentials.append(otherCredentials);
    } finally {
      credentials.writeUnlock(stamp);
    }
  }

  void put(String user, byte[] pass) {
    long stamp = credentials.writeLock();
    try {
      credentials.put(user, pass);
    } finally {
      credentials.writeUnlock(stamp);
    }
  }

  @Override
  public Mono<Boolean> check(String user, byte[] pass) {
    return Mono.just(Arrays.equals(pass, credentials.get(user)));
  }

  @Override
  public Mono<Boolean> check(byte[] pass) {
    return Mono.just(Boolean.FALSE);
  }
}
