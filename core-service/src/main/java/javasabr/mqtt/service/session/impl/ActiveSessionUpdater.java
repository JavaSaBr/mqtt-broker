package javasabr.mqtt.service.session.impl;

import java.io.IOException;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableIntArray;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.common.util.ThreadUtils;

public class ActiveSessionUpdater implements AutoCloseable {
  
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

  public void update() {
    MutableIntArray calculations = ArrayFactory.mutableIntArray();
    while (!closed) {
      ThreadUtils.sleep(updateIntervalInMs);
      if (activeSessions.isEmpty()) {
        continue;
      }
      long stamp = activeSessions.readLock();
      try {
        long currentTimeInMs = System.currentTimeMillis();
        int counter = 0;
        for (InMemoryNetworkMqttSession activeSession : activeSessions) {
          if (counter % 50 == 0) {
            currentTimeInMs = System.currentTimeMillis();
          }
          counter++;
          activeSession.update(currentTimeInMs, calculations);
        }
      } finally {
        activeSessions.readUnlock(stamp);
      }
    }
  }

  @Override
  public void close() throws IOException {
    closed = true;
    updateThread.interrupt();
  }
}
