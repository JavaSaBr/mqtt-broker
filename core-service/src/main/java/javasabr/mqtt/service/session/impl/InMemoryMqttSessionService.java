package javasabr.mqtt.service.session.impl;

import java.io.Closeable;
import java.time.Duration;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.session.MqttSessionService;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.common.util.ThreadUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InMemoryMqttSessionService implements MqttSessionService, Closeable {

  final LockableRefToRefDictionary<String, InMemoryNetworkMqttSession> activeSessions;
  final LockableRefToRefDictionary<String, InMemoryNetworkMqttSession> storedSessions;
  final LockableRefToRefDictionary<String, ExpirableSession> storedExpirableSessions;

  final Thread cleanThread;

  final int cleanIntervalInMs;
  volatile boolean closed;

  public InMemoryMqttSessionService(int cleanIntervalInMs) {
    this.cleanIntervalInMs = cleanIntervalInMs;
    this.activeSessions = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.storedExpirableSessions = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.storedSessions = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.cleanThread = new Thread(this::cleanup, "InMemoryMqttSessionService-Cleanup");
    this.cleanThread.setPriority(Thread.MIN_PRIORITY);
    this.cleanThread.setDaemon(true);
    this.cleanThread.start();
  }

  @Override
  public Mono<NetworkMqttSession> createClean(String clientId) {
    discardStoredSession(clientId);
    // check if we already have an active session
    long stamp = activeSessions.writeLock();
    try {
      InMemoryNetworkMqttSession currentActiveSession = activeSessions.get(clientId);
      if (currentActiveSession != null) {
        throw new IllegalStateException("Client:[%s] already has active session".formatted(clientId));
      }
      InMemoryNetworkMqttSession newCleanSession = new InMemoryNetworkMqttSession(clientId);
      activeSessions.put(clientId, newCleanSession);
      log.debug(clientId, "[%s] Created new clean session"::formatted);
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
        throw new IllegalStateException("Client:[%s] already has active session".formatted(clientId));
      }
    } finally {
      activeSessions.readUnlock(stamp);
    }
    InMemoryNetworkMqttSession restoredSession = tryToRestoreSession(clientId);
    if (restoredSession == null) {
      log.debug(clientId, "[%s] No any stored session"::formatted);
      return Mono.empty();
    }
    stamp = activeSessions.writeLock();
    try {
      InMemoryNetworkMqttSession currentActiveSession = activeSessions.get(clientId);
      if (currentActiveSession != null) {
        throw new IllegalStateException("Client:[%s] already has active session".formatted(clientId));
      }
      activeSessions.put(clientId, restoredSession);
    } finally {
      activeSessions.writeUnlock(stamp);
    }
    return Mono.just(restoredSession);
  }

  @Override
  public Mono<Boolean> store(String clientId, NetworkMqttSession session) {
    // check if we already have an active session
    long stamp = activeSessions.writeLock();
    try {
      InMemoryNetworkMqttSession currentActiveSession = activeSessions.get(clientId);
      if (currentActiveSession != session) {
        throw new IllegalStateException("Client:[%s] has another active session".formatted(clientId));
      }
      activeSessions.remove(clientId);
      Duration expiryInterval = currentActiveSession.expiryInterval();
      if (expiryInterval == MqttProperties.SESSION_EXPIRY_DURATION_DISABLED) {
        return Mono.just(false);
      } else if (expiryInterval == MqttProperties.SESSION_EXPIRY_DURATION_INFINITY) {
        storeNotExpirableSession(clientId, currentActiveSession);
      } else {
        storeExpirableSession(clientId, expiryInterval, currentActiveSession);
      }
      return Mono.just(true);
    } finally {
      activeSessions.writeUnlock(stamp);
    }
  }

  @Override
  public Mono<Boolean> close(String clientId, NetworkMqttSession session) {
    long stamp = activeSessions.writeLock();
    try {
      InMemoryNetworkMqttSession currentActiveSession = activeSessions.get(clientId);
      if (currentActiveSession != session) {
        throw new IllegalStateException("Client:[%s] has another active session".formatted(clientId));
      }
      activeSessions.remove(clientId);
      currentActiveSession.clear();
      return Mono.just(true);
    } finally {
      activeSessions.writeUnlock(stamp);
    }
  }

  @Nullable
  private InMemoryNetworkMqttSession tryToRestoreSession(String clientId) {
    // try to find stored expirable session to restore
    long stamp = storedExpirableSessions.writeLock();
    try {
      ExpirableSession expirableSession = storedExpirableSessions.remove(clientId);
      if (expirableSession != null) {
        log.debug(clientId, "[%s] Restored expirable session"::formatted);
        return expirableSession.session();
      }
    } finally {
      storedExpirableSessions.writeUnlock(stamp);
    }
    // try to find stored not expirable session to restore
    stamp = storedSessions.writeLock();
    try {
      InMemoryNetworkMqttSession notExpirableSession = storedSessions.remove(clientId);
      if (notExpirableSession != null) {
        log.debug(clientId, "[%s] Restored not expirable session"::formatted);
        return notExpirableSession;
      }
    } finally {
      storedSessions.writeUnlock(stamp);
    }
    return null;
  }
  
  private void storeNotExpirableSession(String clientId, InMemoryNetworkMqttSession activeSession) {
    long stamp = storedSessions.writeLock();
    try {
      var previous = storedSessions.put(clientId, activeSession);
      if (previous != null) {
        throw new IllegalStateException("Client:[%s] already has stored not expirable session".formatted(clientId));
      }
      log.info(clientId, "[%s] Stored not expirable session"::formatted);
    } finally {
      storedSessions.writeUnlock(stamp);
    }
  }

  private void storeExpirableSession(
      String clientId,
      Duration expiryInterval,
      InMemoryNetworkMqttSession currentActiveSession) {
    long stamp = storedExpirableSessions.writeLock();
    try {
      var expirableSession = ExpirableSession.of(expiryInterval, currentActiveSession);
      ExpirableSession previous = storedExpirableSessions.put(clientId, expirableSession);
      if (previous != null) {
        throw new IllegalStateException("Client:[%s] already has stored expirable session".formatted(clientId));
      }
      log.info(clientId, expiryInterval, "[%s] Stored expirable session with expiration:[%s]"::formatted);
    } finally {
      storedExpirableSessions.writeUnlock(stamp);
    }
  }

  private void discardStoredSession(String clientId) {
    // try to find stored expirable session to discard
    long stamp = storedExpirableSessions.writeLock();
    try {
      ExpirableSession expirableSession = storedExpirableSessions.remove(clientId);
      if (expirableSession != null) {
        log.debug(clientId, "[%s] Discard expirable session"::formatted);
        expirableSession.session().clear();
        return;
      }
    } finally {
      storedExpirableSessions.writeUnlock(stamp);
    }
    // try to find stored not expirable session to discard
    stamp = storedSessions.writeLock();
    try {
      InMemoryNetworkMqttSession storedSession = storedSessions.remove(clientId);
      if (storedSession != null) {
        log.debug(clientId, "[%s] Discard not expirable session"::formatted);
        storedSession.clear();
        return;
      }
    } finally {
      storedSessions.writeUnlock(stamp);
    }
    log.debug(clientId, "[%s] No any stored session to discard"::formatted);
  }

  private void cleanup() {
    var sessionsToCheck = ArrayFactory.mutableArray(ExpirableSession.class);
    var expiredSessions = ArrayFactory.mutableArray(ExpirableSession.class);
    while (!closed) {
      ThreadUtils.sleep(cleanIntervalInMs);
      if (storedExpirableSessions.isEmpty()) {
        continue;
      }
      long stamp = storedExpirableSessions.readLock();
      try {
        storedExpirableSessions.values(sessionsToCheck);
      } finally {
        storedExpirableSessions.readUnlock(stamp);
      }
      if (sessionsToCheck.isEmpty()) {
        continue;
      }
      collectExpiredSessions(sessionsToCheck, expiredSessions);
      if (!expiredSessions.isEmpty()) {
        closeExpiredSessions(expiredSessions);
        expiredSessions.clear();
      }
      sessionsToCheck.clear();
    }
  }

  private void collectExpiredSessions(
      MutableArray<ExpirableSession> sessionsToCheck,
      MutableArray<ExpirableSession> expiredSessions) {
    long currentTime = System.currentTimeMillis();
    for (ExpirableSession expirableSession : sessionsToCheck) {
      if (expirableSession.expireAfter() < currentTime) {
        expiredSessions.add(expirableSession);
      }
    }
  }

  private void closeExpiredSessions(MutableArray<ExpirableSession> expiredSessions) {
    long stamp = storedExpirableSessions.writeLock();
    try {
      for (ExpirableSession expirableSession : expiredSessions) {
        InMemoryNetworkMqttSession session = expirableSession.session();
        ExpirableSession currentlyStored = storedExpirableSessions.remove(session.clientId());
        // something was changed during this iteration
        if (expirableSession != currentlyStored) {
          if (currentlyStored != null) {
            // return back the other instance of stored session for the same client id
            storedExpirableSessions.put(session.clientId(), currentlyStored);
          }
          continue;
        }
        log.info(session.clientId(), "[%] Removed expired session"::formatted);
        session.clear();
      }
    } finally {
      storedExpirableSessions.writeUnlock(stamp);
    }
  }

  @Override
  public void close() {
    closed = true;
    cleanThread.interrupt();
  }

  private record ExpirableSession(long expireAfter, InMemoryNetworkMqttSession session) {
    private static ExpirableSession of(Duration expiryInterval, InMemoryNetworkMqttSession session) {
      long expireAfter = System.currentTimeMillis() + expiryInterval.toMillis();
      return new ExpirableSession(expireAfter, session);
    }
  }
}
