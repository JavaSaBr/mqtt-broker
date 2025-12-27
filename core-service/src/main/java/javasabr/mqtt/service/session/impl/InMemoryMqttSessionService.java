package javasabr.mqtt.service.session.impl;

import java.io.Closeable;
import java.time.Duration;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.session.MqttSessionService;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
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
  
  final LockableRefToRefDictionary<String, InMemoryNetworkMqttSession> activeSessions;
  final LockableRefToRefDictionary<String, ExpirableSession> storedSessions;
  
  final Thread cleanThread;

  final int cleanIntervalInMs;
  volatile boolean closed;

  public InMemoryMqttSessionService(int cleanIntervalInMs) {
    this.cleanIntervalInMs = cleanIntervalInMs;
    this.activeSessions = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.storedSessions = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.cleanThread = new Thread(this::cleanup, "InMemoryMqttSessionService-Cleanup");
    this.cleanThread.setPriority(Thread.MIN_PRIORITY);
    this.cleanThread.setDaemon(true);
    this.cleanThread.start();
  }

  @Override
  public Mono<NetworkMqttSession> createClean(String clientId) {
    // check if we already have an active session
    long stamp = activeSessions.writeLock();
    try {
      InMemoryNetworkMqttSession currentActiveSession = activeSessions.get(clientId);
      if (currentActiveSession != null) {
        //TODO what should we do here?
      }

      InMemoryNetworkMqttSession newCleanSession = new InMemoryNetworkMqttSession(clientId);
      activeSessions.put(clientId, newCleanSession);
      
      log.debug(clientId, "[%s] Created new session"::formatted);
      return Mono.just(newCleanSession);
    } finally {
      activeSessions.writeUnlock(stamp);
    }
  }

  @Override
  public Mono<NetworkMqttSession> restore(String clientId) {
    // check if we already have an active session
    long stamp = activeSessions.readLock();
    try {
      InMemoryNetworkMqttSession currentActiveSession = activeSessions.get(clientId);
      if (currentActiveSession != null) {
        //TODO what should we do here?
      }
    } finally {
      activeSessions.readUnlock(stamp);
    }
    // try to find restore stored session
    stamp = storedSessions.writeLock();
    try {
      ExpirableSession storedSession = storedSessions.remove(clientId);
      if (storedSession != null) {
        log.debug(clientId, "[%s] Restored session"::formatted);
        return Mono.just(storedSession.session());
      }
    } finally {
      storedSessions.writeUnlock(stamp);
    }
    log.debug(clientId, "[%s] No any stored session"::formatted);
    return Mono.empty();
  }

  @Override
  public Mono<Boolean> store(String clientId, NetworkMqttSession session) {
    // check if we already have an active session
    long stamp = activeSessions.writeLock();
    try {
      InMemoryNetworkMqttSession currentActiveSession = activeSessions.get(clientId);
      if (currentActiveSession != session) {
        //TODO what should we do here?
      }
      activeSessions.remove(clientId);

      Duration expiryInterval = session.expiryInterval();
      if (expiryInterval == null) {
        return Mono.just(false);
      }
      
    } finally {
      activeSessions.writeUnlock(stamp);
    }
    
    return null;
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

    var sessionsToCheck = ArrayFactory.mutableArray(ExpirableSession.class);
    var expiredSessions = ArrayFactory.mutableArray(ExpirableSession.class);

    while (!closed) {
      ThreadUtils.sleep(cleanIntervalInMs);
      if (storedSessions.isEmpty()) {
        continue;
      }

      sessionsToCheck.clear();
      expiredSessions.clear();

      long stamp = storedSessions.readLock();
      try {
        storedSessions.values(sessionsToCheck);
      } finally {
        storedSessions.readUnlock(stamp);
      }
      if (sessionsToCheck.isEmpty()) {
        continue;
      }
      collectExpiredSessions(sessionsToCheck, expiredSessions);
      if (!expiredSessions.isEmpty()) {
        closeExpiredSessions(expiredSessions);
      }
    }
  }
  
  private void collectExpiredSessions(
      MutableArray<ExpirableSession> sessionsToCheck,
      MutableArray<ExpirableSession> expiredSessions) {
    long currentTime = System.currentTimeMillis();
    for (ExpirableSession expirableSession : sessionsToCheck) {
      if (expirableSession.expiredAfter() < currentTime) {
        expiredSessions.add(expirableSession);
      }
    }
  }
  
  private void closeExpiredSessions(MutableArray<ExpirableSession> expiredSessions) {
    long stamp = storedSessions.writeLock();
    try {
      for (ExpirableSession expiredSession : expiredSessions) {
        InMemoryNetworkMqttSession session = expiredSession.session();
        ExpirableSession currentlyStored = storedSessions.remove(session.clientId());
        // something was changed during this iteration
        if (expiredSession != currentlyStored) {
          if (currentlyStored != null) {
            // return back the other instance of stored session for the same client id
            storedSessions.put(session.clientId(), currentlyStored);
          }
          continue;
        }
        log.info(session.clientId(), "[%] Removed expired session"::formatted);
        session.clear();
      }
    } finally {
      storedSessions.writeUnlock(stamp);
    }
  }

  @Override
  public void close() {
    closed = true;
    cleanThread.interrupt();
  }

  private record ExpirableSession(long expiredAfter, InMemoryNetworkMqttSession session) {
    private static ExpirableSession of(Duration expiryInterval, InMemoryNetworkMqttSession session) {
      long expiredAfter = System.currentTimeMillis() + expiryInterval.toMillis();
      return new ExpirableSession(expiredAfter, session);
    }
  }
}
