package javasabr.mqtt.auth.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import javasabr.rlib.collections.dictionary.DictionaryCollectors;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import reactor.core.publisher.Mono;

public abstract class InMemoryCredentialsSource implements CredentialsSource {

  private final LockableRefToRefDictionary<String, byte[]> credentials =
      DictionaryFactory.stampedLockBasedRefToRefDictionary(String.class, byte[].class);

  private void reset(RefToRefDictionary<String, byte[]> otherCredentials) {
    long stamp = credentials.writeLock();
    try {
      credentials.clear();
      credentials.append(otherCredentials);
    } finally {
      credentials.writeUnlock(stamp);
    }
  }

  private void put(String user, byte[] pass) {
    long stamp = credentials.writeLock();
    try {
      credentials.put(user, pass);
    } finally {
      credentials.writeUnlock(stamp);
    }
  }

  protected void reset(InputStream inStream) throws IOException {
    var credentialsProperties = new Properties();
    credentialsProperties.load(inStream);

    var credentials = credentialsProperties.entrySet().stream()
        .collect(DictionaryCollectors.toRefToRefDictionary(
            entry -> entry.getKey().toString(),
            entry -> entry.getValue().toString().getBytes(StandardCharsets.UTF_8)));
    reset(credentials);
  }

  @Override
  public Mono<Boolean> isCredentialsExists(String userName, byte[] password) {
    return Mono.just(Arrays.equals(password, credentials.get(userName)));
  }
}
