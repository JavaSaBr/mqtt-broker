package javasabr.mqtt.service.publish.impl;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.model.publish.SimpleIncomingPublish;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.IntArray;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryIncomingPublishStorage implements IncomingPublishStorage {
  
  LockableRefToRefDictionary<UUID, StoredIncomingPublish> storedPublishes;

  public InMemoryIncomingPublishStorage() {
    this.storedPublishes = DictionaryFactory.stampedLockBasedRefToRefDictionary();
  }

  @Override
  public IncomingPublish store(
      UUID publishId,
      int messageId,
      QoS qos,
      TopicName topicName,
      @Nullable TopicName responseTopicName,
      PublishData data,
      boolean duplicate,
      boolean retain,
      IntArray subscriptionIds,
      long messageExpiryInterval,
      int topicAlias,
      Array<StringPair> userProperties) {
    IncomingPublish incomingPublish;
    long stamp = storedPublishes.writeLock();
    try {
      if (storedPublishes.containsKey(publishId)) {
        throw new IllegalArgumentException("Publish with id:[%s] already exists".formatted(publishId));
      }
      incomingPublish = new SimpleIncomingPublish(
          publishId,
          messageId,
          qos,
          topicName,
          responseTopicName,
          data,
          duplicate,
          retain,
          subscriptionIds,
          messageExpiryInterval,
          topicAlias,
          userProperties);
      storedPublishes.put(incomingPublish.id(), new StoredIncomingPublish(incomingPublish));
    } finally {
      storedPublishes.writeUnlock(stamp);
    }
    log.debug(incomingPublish, "Registered publish in storage: %s"::formatted);
    return incomingPublish;
  }

  @Override
  public void remove(IncomingPublish publish) {
    long stamp = storedPublishes.writeLock();
    try {
      removeWithoutLock(publish);
    } finally {
      storedPublishes.writeUnlock(stamp);
    }
  }

  @Override
  public void removeIfExist(IncomingPublish publish) {
    long stamp = storedPublishes.writeLock();
    try {
      if (storedPublishes.containsKey(publish.id())) {
        removeWithoutLock(publish);
      }
    } finally {
      storedPublishes.writeUnlock(stamp);
    }
  }
  
  private void removeWithoutLock(IncomingPublish publish) {
    StoredIncomingPublish stored = storedPublishes.remove(publish.id());
    if (stored == null) {
      throw new IllegalArgumentException("Unknown publish:[%s]".formatted(publish.id()));
    } else if (stored.consumerCount.get() > 0) {
      log.warning(
          "Removed publish:[%s] still has [%s] consumers".formatted(
          publish.id(),
          stored.consumerCount));
    }
    log.debug(publish, "Removed publish from storage: %s"::formatted);
  }

  @Override
  public void increaseConsumerCount(IncomingPublish publish, int count) {
    if (count < 1) {
      throw new IllegalArgumentException("Consumers count should be positive");
    }
    StoredIncomingPublish storedPublish;
    long stamp = storedPublishes.readLock();
    try {
      storedPublish = storedPublishes.get(publish.id());
      if (storedPublish == null) {
        throw new IllegalArgumentException("Unknown publish:[%s]".formatted(publish.id()));
      }
    } finally {
      storedPublishes.readUnlock(stamp);
    }
    int result = storedPublish
        .consumerCount()
        .addAndGet(count);
    log.debug(result, publish, "Increased consumers to [%s] for publish:[%s]"::formatted);
  }

  @Override
  public void decreaseConsumerCount(IncomingPublish publish, int count) {
    if (count < 1) {
      throw new IllegalArgumentException("Consumers count should be positive");
    }
    StoredIncomingPublish storedPublish;
    long stamp = storedPublishes.readLock();
    try {
      storedPublish = storedPublishes.get(publish.id());
      if (storedPublish == null) {
        throw new IllegalArgumentException("Unknown publish:[%s]".formatted(publish.id()));
      }
    } finally {
      storedPublishes.readUnlock(stamp);
    }
    int result = storedPublish
        .consumerCount()
        .accumulateAndGet(count, (current, delta) -> current - delta);
    log.debug(result, publish, "Decreased consumers to [%s] for publish:[%s]"::formatted);
    if (result == 0) {
      if (!publish.retained()) {
        remove(publish);
      }
    } else if (result < 0) {
      throw new IllegalArgumentException(
          "Unexpected result of decreaseConsumerCount:[%s] for publish:[%s]".formatted(result, publish.id()));
    }
  }

  @Getter
  @RequiredArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  private static class StoredIncomingPublish {
    final IncomingPublish publish;
    final AtomicInteger consumerCount = new AtomicInteger(0);
  }
}
