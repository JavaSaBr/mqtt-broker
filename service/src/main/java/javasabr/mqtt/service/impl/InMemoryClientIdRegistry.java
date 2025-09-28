package javasabr.mqtt.service.impl;

import javasabr.mqtt.service.ClientIdRegistry;
import java.util.BitSet;
import java.util.UUID;
import javasabr.rlib.collections.dictionary.Dictionary;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.MutableRefToRefDictionary;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryClientIdRegistry implements ClientIdRegistry {

  private static final Object CLIENT_ID_VALUE = new Object();

  LockableRefToRefDictionary<String, Object> clientIdRegistry;
  BitSet availableCharSet;

  int maxClientIdLength;

  public InMemoryClientIdRegistry(String availableChars, int maxClientIdLength) {
    this.maxClientIdLength = maxClientIdLength;
    this.clientIdRegistry = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.availableCharSet = new BitSet();
    for (char ch : availableChars.toCharArray()) {
      availableCharSet.set(ch, true);
    }
  }

  @Override
  public Mono<Boolean> register(String clientId) {

    if (!validate(clientId)) {
      return Mono.just(false);
    }

    var wasAdded = clientIdRegistry
        .operations()
        .getInWriteLock(
            clientId, (registry, id) -> {
              if (registry.containsKey(id)) {
                return false;
              }
              registry.put(id, CLIENT_ID_VALUE);
              return true;
            });

    return Mono.just(wasAdded);
  }

  @Override
  public Mono<Boolean> unregister(String clientId) {
    Object removedValue = clientIdRegistry
        .operations()
        .getInWriteLock(clientId, MutableRefToRefDictionary::remove);
    return Mono.just(removedValue != null);
  }

  @Override
  public boolean validate(String clientId) {

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
  public Mono<String> generate() {
    while (true) {
      String clientId = UUID.randomUUID().toString();
      boolean contains = clientIdRegistry
          .operations()
          .getBooleanInReadLock(clientId, Dictionary::containsKey);
      if (!contains) {
        return Mono.just(clientId);
      }
    }
  }
}
