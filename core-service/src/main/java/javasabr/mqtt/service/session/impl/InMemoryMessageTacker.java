package javasabr.mqtt.service.session.impl;

import java.time.Duration;
import java.util.concurrent.locks.StampedLock;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import javasabr.mqtt.model.session.exception.AlreadyRegisteredMessageMetaException;
import javasabr.mqtt.model.session.exception.NotFoundMessageMetaException;
import javasabr.rlib.collections.array.MutableIntArray;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.MutableIntToRefDictionary;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryMessageTacker implements MessageTacker {

  MutableIntToRefDictionary<InMemoryTrackedMessageMeta> messageIdToMeta;
  StampedLock lock;

  public InMemoryMessageTacker() {
    this.messageIdToMeta = DictionaryFactory.mutableIntToRefDictionary();
    this.lock = new StampedLock();
  }

  @Nullable
  @Override
  public TrackedMessageMeta stored(int messageId) {
    long stamp = lock.readLock();
    try {
      return messageIdToMeta.get(messageId);
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
    add(messageId, messageType, reasonCode, null);
  }

  @Override
  public void add(
      int messageId,
      MqttMessageType messageType,
      @Nullable ReasonCode reasonCode,
      @Nullable Duration expiration) {
    InMemoryTrackedMessageMeta messageMeta = new InMemoryTrackedMessageMeta(
        messageType,
        reasonCode,
        messageId, 
        expiration == null ? 0L : System.currentTimeMillis() + expiration.toMillis());
    long stamp = lock.writeLock();
    try {
      InMemoryTrackedMessageMeta exists = messageIdToMeta.get(messageId);
      if (exists != null) {
        throw new AlreadyRegisteredMessageMetaException(
            messageId,
            "Message meta:[%s] is already registered.".formatted(messageId));
      } messageIdToMeta.put(messageId, messageMeta);
    } finally {
      lock.unlockWrite(stamp);
    }
    log.debug(messageMeta, "Registered new message meta: %s"::formatted);
  }

  @Override
  public TrackedMessageMeta update(int messageId, MqttMessageType messageType, @Nullable ReasonCode reasonCode) {
    long stamp = lock.writeLock();
    try {
      InMemoryTrackedMessageMeta current = messageIdToMeta.get(messageId);
      if (current == null) {
        throw new NotFoundMessageMetaException(messageId, "Message meta:[%s] doesn't exist.".formatted(messageId));
      }
      current.messageType(messageType);
      current.reasonCode(reasonCode);
      log.debug(current, "Updated message meta: %s"::formatted);
      return current;
    } finally {
      lock.unlockWrite(stamp);
    }
  }
  
  @Override
  public TrackedMessageMeta remove(int messageId) {
    TrackedMessageMeta removed;
    long stamp = lock.writeLock();
    try {
      removed = messageIdToMeta.remove(messageId);
      if (removed == null) {
        throw new NotFoundMessageMetaException(messageId, "Message meta:[%s] doesn't exist.".formatted(messageId));
      }
    } finally {
      lock.unlockWrite(stamp);
    }
    log.debug(removed, "Removed message meta: %s"::formatted);
    return removed;
  }
  
  @Nullable
  @Override
  public TrackedMessageMeta removeIfExist(int messageId) {
    TrackedMessageMeta removed;
    long stamp = lock.writeLock();
    try {
      removed = messageIdToMeta.remove(messageId);
    } finally {
      lock.unlockWrite(stamp);
    }
    log.debug(removed, "Removed if exist message meta: %s"::formatted);
    return removed;
  }

  public void clear() {
    long stamp = lock.writeLock();
    try {
      messageIdToMeta.clear();
    } finally {
      lock.unlockWrite(stamp);
    }
  }
  
  public void cleanupExpired(long currentTimeInMs, MutableIntArray calculation) {
    if (messageIdToMeta.isEmpty()) {
      return;
    }
    long stamp = lock.readLock();
    try {
      for (InMemoryTrackedMessageMeta messageMeta : messageIdToMeta) {
        if (messageMeta.expiredAt() > 0 && messageMeta.expiredAt() < currentTimeInMs) {
          calculation.add(messageMeta.messageId());
        }
      }
    } finally {
      lock.unlockRead(stamp);
    }
    if (!calculation.isEmpty()) {
      log.debug(calculation, "Found expired message meta to remove:%s"::formatted);
      stamp = lock.writeLock();
      try {
        for (int i = 0, size = calculation.size(); i < size; i++) {
          int messageId = calculation.get(i);
          InMemoryTrackedMessageMeta exist = messageIdToMeta.get(messageId);
          if (exist != null && exist.expiredAt() > 0 && exist.expiredAt() < currentTimeInMs) {
            messageIdToMeta.remove(messageId);
          }
        }
      } finally {
        lock.unlockWrite(stamp);
      }
      calculation.clear();
    }
  }
}
