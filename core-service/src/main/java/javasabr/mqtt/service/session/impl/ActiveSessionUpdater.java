package javasabr.mqtt.service.session.impl;

import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.common.util.ThreadUtils;
import lombok.CustomLog;

@CustomLog
public class ActiveSessionUpdater implements AutoCloseable {

  public static final int SESSIONS_PART_SIZE = 100;
  final LockableRefToRefDictionary<String, InMemoryNetworkMqttSession> activeSessions;
  final Thread updateThread;

  final int updateIntervalInMs;
  volatile boolean closed;

  public ActiveSessionUpdater(LockableRefToRefDictionary<String, InMemoryNetworkMqttSession> activeSessions,
                              int updateIntervalInMs) {
    this.activeSessions = activeSessions;
    this.updateIntervalInMs = updateIntervalInMs;
    this.updateThread = new Thread(this::update, "ActiveSessionUpdater-Update");
    this.updateThread.setPriority(Thread.MIN_PRIORITY);
    this.updateThread.setDaemon(true);
    this.updateThread.start();
  }

  private void update() {
    var calculations = ArrayFactory.mutableIntArray();
    var sessions = ArrayFactory.mutableArray(InMemoryNetworkMqttSession.class);
    while (!closed) {
      if (ThreadUtils.sleep(updateIntervalInMs)) {
        continue;
      } else if (activeSessions.isEmpty()) {
        continue;
      }
      int partIndex = 0;
      while (partIndex >= 0) {
        long stamp = activeSessions.readLock();
        try {
          partIndex = activeSessions.values(sessions, partIndex, SESSIONS_PART_SIZE);
        } finally {
          activeSessions.readUnlock(stamp);
        }
        if (!sessions.isEmpty()) {
          long currentTimeInMs = System.currentTimeMillis();
          for (InMemoryNetworkMqttSession activeSession : sessions) {
            activeSession.update(currentTimeInMs, calculations);
          }
          sessions.clear();
        }
      }
    }
  }

  @Override
  public void close() {
    closed = true;
    updateThread.interrupt();
  }
}
