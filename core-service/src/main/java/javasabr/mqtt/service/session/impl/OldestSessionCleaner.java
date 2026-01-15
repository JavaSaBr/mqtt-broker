package javasabr.mqtt.service.session.impl;

import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class OldestSessionCleaner<T extends NotExpirableSession> {

  MutableArray<T> sessionsToCheck = ArrayFactory.mutableArray(NotExpirableSession.class);
  MutableArray<T> sessionsToCleanup = ArrayFactory.mutableArray(NotExpirableSession.class);
  
  LockableRefToRefDictionary<String, T> sessions;
  int limit;
  int cleanupBatchSize;
  
  public synchronized void cleanup() {
    if (sessions.size() < limit) {
      return;
    }
    long stamp = sessions.readLock();
    try {
      sessions.values(sessionsToCheck);
    } finally {
      sessions.readUnlock(stamp);
    }
    int foundSessions = sessionsToCheck.size();
    if (foundSessions < limit) {
      sessionsToCheck.clear();
      return;
    }

    long youngest = 0;
    int index = 0;
    int cleanupSize = Math.min(Math.max(foundSessions - limit, cleanupBatchSize), foundSessions);

    // initial fill the array for removing sessions
    for (; index < cleanupSize; index++) {
      T session = sessionsToCheck.get(index);
      sessionsToCleanup.add(session);
      youngest = Math.max(youngest, session.storedAt());
    }

    // check the rest sessions to find older sessions than in the initial array
    for (; index < foundSessions; index++) {
      T session = sessionsToCheck.get(index);
      long storedAt = session.storedAt();
      if (storedAt < youngest) {
        continue;
      }
      // replace one from the initial array to the older session
      long nextYoungest = 0;
      for (int i = 0, size = sessionsToCheck.size(); i < size; i++) {
        T sessionToCheck = sessionsToCheck.get(i);
        if (sessionToCheck.storedAt() == youngest) {
          nextYoungest = Math.max(storedAt, nextYoungest);
          sessionsToCleanup.replace(i, session);
        } else {
          nextYoungest = Math.max(sessionToCheck.storedAt(), nextYoungest);
        }
      }
      youngest = nextYoungest;
    }

    // remove found sessions
    stamp = sessions.writeLock();
    try {
      for (T expectedStoredSession : sessionsToCleanup) {
        InMemoryNetworkMqttSession session = expectedStoredSession.session();
        T current = sessions.get(session.clientId());
        if (current == expectedStoredSession) {
          sessions.remove(session.clientId());
        }
      }
    } finally {
      sessions.writeUnlock(stamp);
    }

    sessionsToCheck.clear();
    sessionsToCleanup.clear();
  }
}
