package javasabr.mqtt.service.session.impl;

import java.util.concurrent.locks.StampedLock;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.MutableIntToRefDictionary;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryMessageTacker implements MessageTacker {

  MutableIntToRefDictionary<InMemoryTrackedMessageMeta> usedMessageIds;
  StampedLock lock;

  public InMemoryMessageTacker() {
    this.usedMessageIds = DictionaryFactory.mutableIntToRefDictionary();
    this.lock = new StampedLock();
  }

  @Nullable
  @Override
  public TrackedMessageMeta stored(int messageId) {
    long stamp = lock.readLock();
    try {
      return usedMessageIds.get(messageId);
    } finally {
      lock.unlockRead(stamp);
    }
  }

  @Override
  public void add(int messageId, MqttMessageType messageType) {
    add(messageId, messageType, null);
  }

  @Override
  public void add(int messageId, MqttMessageType messageType, @Nullable ReasonCode reasonCode) {
    long stamp = lock.writeLock();
    try {
      usedMessageIds.put(messageId, new InMemoryTrackedMessageMeta(messageType, reasonCode));
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Override
  public boolean update(int messageId, MqttMessageType messageType, @Nullable ReasonCode reasonCode) {
    long stamp = lock.writeLock();
    try {
      InMemoryTrackedMessageMeta current = usedMessageIds.get(messageId);
      if (current != null) {
        current.messageType(messageType);
        current.reasonCode(reasonCode);
        return false;
      }
      usedMessageIds.put(messageId, new InMemoryTrackedMessageMeta(messageType, reasonCode));
      return true;
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Nullable
  @Override
  public TrackedMessageMeta remove(int messageId) {
    long stamp = lock.writeLock();
    try {
      return usedMessageIds.remove(messageId);
    } finally {
      lock.unlockWrite(stamp);
    }
  }
}
