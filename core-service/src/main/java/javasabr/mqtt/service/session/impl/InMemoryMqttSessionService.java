package javasabr.mqtt.service.session.impl;

import java.io.Closeable;
import javasabr.mqtt.network.session.ConfigurableNetworkMqttSession;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.session.MqttSessionService;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.collections.dictionary.Dictionary;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.MutableRefToRefDictionary;
import javasabr.rlib.common.util.ThreadUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InMemoryMqttSessionService implements MqttSessionService, Closeable {

  final LockableRefToRefDictionary<String, InMemoryNetworkMqttSession> storedSession;
  final Thread cleanThread;

  final int cleanIntervalInMs;
  volatile boolean closed;

  public InMemoryMqttSessionService(int cleanIntervalInMs) {
    this.cleanIntervalInMs = cleanIntervalInMs;
    this.storedSession = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.cleanThread = new Thread(this::cleanup, "InMemoryMqttSessionService-Cleanup");
    this.cleanThread.setPriority(Thread.MIN_PRIORITY);
    this.cleanThread.setDaemon(true);
    this.cleanThread.start();
  }

  @Override
  public Mono<NetworkMqttSession> restore(String clientId) {

    InMemoryNetworkMqttSession session = storedSession
        .operations()
        .getInWriteLock(clientId, MutableRefToRefDictionary::remove);

    if (session != null) {
      log.debug(clientId, "[%s] Restored session"::formatted);
    } else {
      log.debug(clientId, "[%s] No any stored session"::formatted);
    }

    return Mono.justOrEmpty(session);
  }

  @Override
  public Mono<NetworkMqttSession> create(String clientId) {

    InMemoryNetworkMqttSession session = storedSession
        .operations()
        .getInWriteLock(clientId, MutableRefToRefDictionary::remove);

    if (session != null) {
      log.debug(clientId, "Removed old session for client:[%s]"::formatted);
    }

    log.debug(clientId, "Created new session for client:[%s]"::formatted);

    return Mono.just(new InMemoryNetworkMqttSession(clientId));
  }

  @Override
  public Mono<Boolean> store(String clientId, NetworkMqttSession session, long expiryInterval) {

    var configurable = (InMemoryNetworkMqttSession) session;
    configurable.expirationTime(System.currentTimeMillis() + (expiryInterval * 1000));

    storedSession
        .operations()
        .inWriteLock(clientId, configurable, MutableRefToRefDictionary::put);

    log.debug(clientId, "Stored session for client:[%s]"::formatted);

    return Mono.just(Boolean.TRUE);
  }

  private void cleanup() {

    var toCheck = ArrayFactory.mutableArray(InMemoryNetworkMqttSession.class);
    var toRemove = ArrayFactory.mutableArray(InMemoryNetworkMqttSession.class);

    while (!closed) {
      ThreadUtils.sleep(cleanIntervalInMs);

      toCheck.clear();
      toRemove.clear();

      storedSession
          .operations()
          .inReadLock(toCheck, Dictionary::values);

      if (findToRemove(toCheck, toRemove)) {
        continue;
      }

      storedSession
          .operations()
          .inWriteLock(toRemove, InMemoryMqttSessionService::removeExpiredSessions);
    }
  }

  private static void removeExpiredSessions(
      LockableRefToRefDictionary<String, InMemoryNetworkMqttSession> sessions,
      MutableArray<InMemoryNetworkMqttSession> expired) {
    long time = System.currentTimeMillis();
    for (ConfigurableNetworkMqttSession session : expired) {
      if (session.expirationTime() <= time) {
        continue;
      }

      InMemoryNetworkMqttSession removed = sessions.remove(session.clientId());
      log.debug(session.clientId(), "Removed expired session for client:[%]"::formatted);

      // if we already have new session under the same client id
      if (removed != null && removed != session) {
        sessions.put(session.clientId(), removed);
      } else if (removed != null) {
        removed.clear();
      }
    }
  }

  private boolean findToRemove(
      MutableArray<InMemoryNetworkMqttSession> toCheck, 
      MutableArray<InMemoryNetworkMqttSession> toRemove) {

    var currentTime = System.currentTimeMillis();

    for (InMemoryNetworkMqttSession session : toCheck) {
      if (session.expirationTime() > currentTime) {
        toRemove.add(session);
      }
    }

    return toRemove.isEmpty();
  }

  @Override
  public void close() {
    closed = true;
    cleanThread.interrupt();
  }
}
