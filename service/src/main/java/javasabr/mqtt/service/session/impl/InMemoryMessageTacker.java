package javasabr.mqtt.service.session.impl;

import java.util.concurrent.locks.StampedLock;
import javasabr.mqtt.network.session.MessageTacker;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableIntArray;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryMessageTacker implements MessageTacker {

  MutableIntArray usedMessageIds;
  StampedLock lock;

  public InMemoryMessageTacker() {
    this.usedMessageIds = ArrayFactory.mutableIntArray();
    this.lock = new StampedLock();
  }

  @Override
  public boolean isInUse(int messageId) {
    long stamp = lock.readLock();
    try {
      return usedMessageIds.contains(messageId);
    } finally {
      lock.unlockRead(stamp);
    }
  }

  @Override
  public void add(int messageId) {
    long stamp = lock.writeLock();
    try {
      usedMessageIds.add(messageId);
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Override
  public void remove(int messageId) {
    long stamp = lock.writeLock();
    try {
      usedMessageIds.remove(messageId);
    } finally {
      lock.unlockWrite(stamp);
    }
  }
}
