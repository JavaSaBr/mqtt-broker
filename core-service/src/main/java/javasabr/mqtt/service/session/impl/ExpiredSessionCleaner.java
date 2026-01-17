package javasabr.mqtt.service.session.impl;

import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class ExpiredSessionCleaner {

  MutableArray<ExpirableSession> sessionsToCheck = ArrayFactory.mutableArray(ExpirableSession.class);
  MutableArray<ExpirableSession> expiredSessions = ArrayFactory.mutableArray(ExpirableSession.class);

  LockableRefToRefDictionary<String, ExpirableSession> sessions;

  public synchronized void cleanup() {
    if (sessions.isEmpty()) {
      return;
    }
    long stamp = sessions.readLock();
    try {
      sessions.values(sessionsToCheck);
    } finally {
      sessions.readUnlock(stamp);
    }
    if (sessionsToCheck.isEmpty()) {
      return;
    }
    collectExpiredSessions();
    if (!expiredSessions.isEmpty()) {
      deleteExpiredSessions();
      expiredSessions.clear();
    }
    sessionsToCheck.clear();
  }

  private void collectExpiredSessions() {
    long currentTime = System.currentTimeMillis();
    for (ExpirableSession expirableSession : sessionsToCheck) {
      if (expirableSession.expireAfter() < currentTime) {
        expiredSessions.add(expirableSession);
      }
    }
  }

  private void deleteExpiredSessions() {
    long stamp = sessions.writeLock();
    try {
      for (ExpirableSession expirableSession : expiredSessions) {
        InMemoryNetworkMqttSession session = expirableSession.session();
        ExpirableSession currentlyStored = sessions.get(session.clientId());
        if (expirableSession == currentlyStored) {
          // nothing was changed during this iteration
          log.info(session.clientId(), "[%] Removed expired session"::formatted);
          sessions.remove(session.clientId());
          session.clear();
        }
      }
    } finally {
      sessions.writeUnlock(stamp);
    }
  }
}
