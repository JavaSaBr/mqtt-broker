package javasabr.mqtt.service.session.impl;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.session.MqttSessionService;
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

  public static final int HARD_SESSIONS_LIMIT = 500;
  public static final int MAX_SESSIONS = 300;
  public static final int CLEANUP_BATCH_SIZE = 30;
  
  final LockableRefToRefDictionary<String, InMemoryNetworkMqttSession> activeSessions;
  final LockableRefToRefDictionary<String, NotExpirableSession> storedNotExpirableSessions;
  final LockableRefToRefDictionary<String, ExpirableSession> storedExpirableSessions;

  final AtomicLong internalIdGenerator;
  final Thread cleanupThread;
  final OldestSessionCleaner<ExpirableSession> expirableOldestSessionCleaner;
  final OldestSessionCleaner<NotExpirableSession> notExpirableOldestSessionCleaner;
  final ExpiredSessionCleaner expiredSessionCleaner;
  final ActiveSessionUpdater activeSessionUpdater;

  final int cleanupIntervalInMs;
  final int hardSessionsLimit;
  
  volatile boolean closed;

  public InMemoryMqttSessionService(int cleanupIntervalInMs, int updateIntervalInMs) {
    this(
        cleanupIntervalInMs, 
        updateIntervalInMs, 
        MAX_SESSIONS,
        MAX_SESSIONS, 
        HARD_SESSIONS_LIMIT,
        CLEANUP_BATCH_SIZE);
  }
  
  public InMemoryMqttSessionService(
      int cleanupIntervalInMs,
      int updateIntervalInMs,
      int maxNotExpirableSessions,
      int maxExpirableStoredSessions,
      int hardSessionsLimit,
      int cleanupBatchSize) {
    this.cleanupIntervalInMs = cleanupIntervalInMs;
    this.hardSessionsLimit = hardSessionsLimit;
    this.activeSessions = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.storedExpirableSessions = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.storedNotExpirableSessions = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.internalIdGenerator = new AtomicLong(0);
    this.expirableOldestSessionCleaner = new OldestSessionCleaner<>(
        storedExpirableSessions, 
        maxExpirableStoredSessions, 
        cleanupBatchSize);
    this.notExpirableOldestSessionCleaner = new OldestSessionCleaner<>(
        storedNotExpirableSessions, 
        maxNotExpirableSessions,
        cleanupBatchSize);
    this.expiredSessionCleaner = new ExpiredSessionCleaner(storedExpirableSessions);
    this.activeSessionUpdater = new ActiveSessionUpdater(activeSessions, updateIntervalInMs);
    this.cleanupThread = new Thread(this::cleanup, "InMemoryMqttSessionService-Cleanup");
    this.cleanupThread.setPriority(Thread.MIN_PRIORITY);
    this.cleanupThread.setDaemon(true);
    this.cleanupThread.start();
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
      InMemoryNetworkMqttSession newCleanSession = new InMemoryNetworkMqttSession(
          clientId,
          internalIdGenerator.incrementAndGet());
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
    } finally {
      activeSessions.writeUnlock(stamp);
    }
    InMemoryNetworkMqttSession storableSession = (InMemoryNetworkMqttSession) session;
    Duration expiryInterval = session.expiryInterval();
    if (expiryInterval == MqttProperties.SESSION_EXPIRY_DURATION_DISABLED) {
      return Mono.just(false);
    } else if (expiryInterval == MqttProperties.SESSION_EXPIRY_DURATION_INFINITY) {
      storeNotExpirableSession(clientId, storableSession);
    } else {
      storeExpirableSession(clientId, expiryInterval, storableSession);
    }
    return Mono.just(true);
  }

  @Override
  public Mono<Boolean> delete(String clientId, NetworkMqttSession session) {
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
        return expirableSession.wrapped();
      }
    } finally {
      storedExpirableSessions.writeUnlock(stamp);
    }
    // try to find stored not expirable session to restore
    stamp = storedNotExpirableSessions.writeLock();
    try {
      NotExpirableSession notExpirableSession = storedNotExpirableSessions.remove(clientId);
      if (notExpirableSession != null) {
        log.debug(clientId, "[%s] Restored not expirable session"::formatted);
        return notExpirableSession.wrapped();
      }
    } finally {
      storedNotExpirableSessions.writeUnlock(stamp);
    }
    return null;
  }
  
  private void storeNotExpirableSession(String clientId, InMemoryNetworkMqttSession activeSession) {
    if (storedNotExpirableSessions.size() > hardSessionsLimit) {
      notExpirableOldestSessionCleaner.cleanup();
    }
    long stamp = storedNotExpirableSessions.writeLock();
    try {
      NotExpirableSession previous = storedNotExpirableSessions.get(clientId);
      if (previous != null) {
        throw new IllegalStateException("Client:[%s] already has stored not expirable session".formatted(clientId));
      }
      storedNotExpirableSessions.put(clientId, NotExpirableSession.of(activeSession));
      log.info(clientId, "[%s] Stored not expirable session"::formatted);
    } finally {
      storedNotExpirableSessions.writeUnlock(stamp);
    }
  }

  private void storeExpirableSession(
      String clientId,
      Duration expiryInterval,
      InMemoryNetworkMqttSession currentActiveSession) {
    if (storedExpirableSessions.size() > hardSessionsLimit) {
      expirableOldestSessionCleaner.cleanup();
    }
    long stamp = storedExpirableSessions.writeLock();
    try {
      ExpirableSession previous = storedExpirableSessions.get(clientId);
      if (previous != null) {
        throw new IllegalStateException("Client:[%s] already has stored expirable session".formatted(clientId));
      }
      storedExpirableSessions.put(clientId, ExpirableSession.of(expiryInterval, currentActiveSession));
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
        expirableSession.wrapped().clear();
        return;
      }
    } finally {
      storedExpirableSessions.writeUnlock(stamp);
    }
    // try to find stored not expirable session to discard
    stamp = storedNotExpirableSessions.writeLock();
    try {
      NotExpirableSession storedSession = storedNotExpirableSessions.remove(clientId);
      if (storedSession != null) {
        log.debug(clientId, "[%s] Discard not expirable session"::formatted);
        storedSession.wrapped().clear();
        return;
      }
    } finally {
      storedNotExpirableSessions.writeUnlock(stamp);
    }
    log.debug(clientId, "[%s] No any stored session to discard"::formatted);
  }

  private void cleanup() {
    while (!closed) {
      if (ThreadUtils.sleep(cleanupIntervalInMs)) {
        continue;
      }
      expiredSessionCleaner.cleanup();
      expirableOldestSessionCleaner.cleanup();
      notExpirableOldestSessionCleaner.cleanup();
    }
  }
  
  @Override
  public void close() throws IOException {
    closed = true;
    cleanupThread.interrupt();
    activeSessionUpdater.close();
  }
}
