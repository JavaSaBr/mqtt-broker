package javasabr.mqtt.service.session.impl;

import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Selects and removes the oldest stored sessions when the total number of sessions exceeds a configured
 * {@link #limit}.
 * <p>
 * The cleaner works in two phases:
 * <ol>
 *   <li>It takes a snapshot of all current sessions under a read lock and populates
 *   {@link #sessionsToCheck}.</li>
 *   <li>It then selects up to {@code extraSessions + cleanupBatchSize} candidates to
 *   remove, where {@code extraSessions = currentSessions - limit}. The initial
 *   candidate set is filled from the first {@code cleanupSize} sessions and tracks
 *   the "youngest" {@code storedAt} timestamp among them. While scanning the
 *   remaining sessions, any session older than or equal to the current youngest
 *   candidate replaces a younger candidate, and the youngest boundary is updated.
 *   This yields a small set containing the oldest sessions without fully sorting
 *   the entire collection.</li>
 * </ol>
 * After the candidate set is built, the cleaner acquires a write lock and removes
 * only those candidates that are still present and unchanged in {@link #sessions}.
 * <p>
 * Parameters:
 * <ul>
 *   <li>{@link #limit} – maximum allowed number of stored sessions. If the current
 *   number of sessions is below this value, {@link #cleanup()} is a no-op.</li>
 *   <li>{@link #cleanupBatchSize} – number of additional sessions (beyond the
 *   minimum required {@code extraSessions}) considered during each cleanup run.
 *   A larger batch size means more candidates are examined per run, which can
 *   reduce how often cleanup is needed at the cost of slightly more work per call.</li>
 * </ul>
 * <p>
 * Type parameter {@code <T>} is the type of stored session, which must expose a stable
 * {@link NotExpirableSession#storedAt()} timestamp and an underlying
 * {@link InMemoryNetworkMqttSession} used for lookup/removal.
 */
@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class OldestSessionCleaner<T extends NotExpirableSession> {

  MutableArray<T> sessionsToCheck = ArrayFactory.mutableArray(NotExpirableSession.class);
  MutableArray<T> sessionsToCleanup = ArrayFactory.mutableArray(NotExpirableSession.class);
  
  LockableRefToRefDictionary<String, T> sessions;
  int limit;
  int cleanupBatchSize;
  
  public synchronized void cleanup() {
    if (sessions.size() <= limit) {
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
    int extraSessions = foundSessions - limit;
    int cleanupSize = Math.min(extraSessions + cleanupBatchSize, foundSessions);

    // Initially fill the array for removing sessions
    for (; index < cleanupSize; index++) {
      T session = sessionsToCheck.get(index);
      sessionsToCleanup.add(session);
      youngest = Math.max(youngest, session.storedAt());
    }

    // check the rest sessions to find older sessions than in the initial array
    for (; index < foundSessions; index++) {
      T session = sessionsToCheck.get(index);
      long storedAt = session.storedAt();
      if (storedAt > youngest) {
        continue;
      }
      // replace one from the initial array to the older session
      long nextYoungest = 0;
      boolean replaced = false;
      for (int i = 0, size = sessionsToCleanup.size(); i < size; i++) {
        T sessionToCheck = sessionsToCleanup.get(i);
        if (sessionToCheck.storedAt() == youngest && !replaced) {
          nextYoungest = Math.max(storedAt, nextYoungest);
          sessionsToCleanup.replace(i, session);
          replaced = true;
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
        InMemoryNetworkMqttSession wrapped = expectedStoredSession.wrapped();
        String clientId = wrapped.clientId();
        if (sessions.remove(clientId, expectedStoredSession)) {
          log.info(clientId, "[%s] Removed oldest session"::formatted);
          wrapped.clear();
        }
      }
    } finally {
      sessions.writeUnlock(stamp);
    }

    sessionsToCheck.clear();
    sessionsToCleanup.clear();
  }
}
