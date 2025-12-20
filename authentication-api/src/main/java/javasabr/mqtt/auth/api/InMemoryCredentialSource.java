package javasabr.mqtt.auth.api;

import java.util.Arrays;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import reactor.core.publisher.Mono;

public abstract class InMemoryCredentialSource implements CredentialSource {

  private final LockableRefToRefDictionary<String, byte[]> credentials =
      DictionaryFactory.stampedLockBasedRefToRefDictionary(String.class, byte[].class);

  protected abstract void init();

  protected void reset(RefToRefDictionary<String, byte[]> otherCredentials) {
    long stamp = credentials.writeLock();
    try {
      credentials.clear();
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
  public Mono<Boolean> isCredentialExists(String user, byte[] pass) {
    return Mono.just(Arrays.equals(pass, credentials.get(user)));
  }
}
