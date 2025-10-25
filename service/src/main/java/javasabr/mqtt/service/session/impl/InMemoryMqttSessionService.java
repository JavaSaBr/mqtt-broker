package javasabr.mqtt.service.session.impl;

import java.io.Closeable;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.MqttSession.UnsafeMqttSession;
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

  final LockableRefToRefDictionary<String, UnsafeMqttSession> storedSession;
  final Thread cleanThread;

  final int cleanInterval;
  volatile boolean closed;

  public InMemoryMqttSessionService(int cleanInterval) {
    this.cleanInterval = cleanInterval;
    this.storedSession = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.cleanThread = new Thread(this::cleanup, "InMemoryMqttSessionService-Cleanup");
    this.cleanThread.setPriority(Thread.MIN_PRIORITY);
    this.cleanThread.setDaemon(true);
    this.cleanThread.start();
  }

  @Override
  public Mono<MqttSession> restore(String clientId) {

    UnsafeMqttSession session = storedSession
        .operations()
        .getInWriteLock(clientId, MutableRefToRefDictionary::remove);

    if (session != null) {
      session.onRestored();
      log.debug(clientId, "Restored session for client:[%s]"::formatted);
    } else {
      log.debug(clientId, "No stored session for client:[%s]"::formatted);
    }

    return Mono.justOrEmpty(session);
  }

  @Override
  public Mono<MqttSession> create(String clientId) {

    UnsafeMqttSession session = storedSession
        .operations()
        .getInWriteLock(clientId, MutableRefToRefDictionary::remove);

    if (session != null) {
      log.debug(clientId, "Removed old session for client:[%s]"::formatted);
    }

    log.debug(clientId, "Created new session for client:[%s]"::formatted);

    return Mono.just(new InMemoryMqttSession(clientId));
  }

  @Override
  public Mono<Boolean> store(String clientId, MqttSession session, long expiryInterval) {

    var unsafe = (UnsafeMqttSession) session;
    unsafe.expirationTime(System.currentTimeMillis() + (expiryInterval * 1000));
    unsafe.onPersisted();

    storedSession
        .operations()
        .inWriteLock(clientId, unsafe, MutableRefToRefDictionary::put);

    log.debug(clientId, "Stored session for client:[%s]"::formatted);

    return Mono.just(Boolean.TRUE);
  }

  private void cleanup() {

    var toCheck = ArrayFactory.mutableArray(UnsafeMqttSession.class);
    var toRemove = ArrayFactory.mutableArray(UnsafeMqttSession.class);

    while (!closed) {
      ThreadUtils.sleep(cleanInterval);

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
      LockableRefToRefDictionary<String, UnsafeMqttSession> sessions,
      MutableArray<UnsafeMqttSession> expired) {
    long time = System.currentTimeMillis();
    for (UnsafeMqttSession session : expired) {
      if (session.expirationTime() <= time) {
        continue;
      }

      UnsafeMqttSession removed = sessions.remove(session.clientId());
      log.debug(session.clientId(), "Removed expired session for client:[%]"::formatted);

      // if we already have new session under the same client id
      if (removed != null && removed != session) {
        sessions.put(session.clientId(), removed);
      } else if (removed != null) {
        removed.clear();
      }
    }
  }

  private boolean findToRemove(MutableArray<UnsafeMqttSession> toCheck, MutableArray<UnsafeMqttSession> toRemove) {

    var currentTime = System.currentTimeMillis();

    for (UnsafeMqttSession session : toCheck) {
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
