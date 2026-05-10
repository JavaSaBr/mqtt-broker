package javasabr.mqtt.service.publish.impl;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.model.publish.SimpleIncomingPublish;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.service.publish.IncomingPublishStorage;
import javasabr.mqtt.service.publish.PublishDataStorage;
import javasabr.mqtt.service.publish.exception.AlreadyRemovedPublishStorageException;
import javasabr.mqtt.service.publish.exception.AlreadyScheduledPublishStorageException;
import javasabr.mqtt.service.publish.exception.UnknownPublishStorageException;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.IntArray;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.MutableRefToRefDictionary;
import javasabr.rlib.common.util.ThreadUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InMemoryIncomingPublishStorage implements IncomingPublishStorage, Closeable {

  final PublishDataStorage publishDataStorage;
  final LockableRefToRefDictionary<UUID, StoredIncomingPublish> storedPublishes;
  final LockableRefToRefDictionary<UUID, ScheduledForRemovalIncomingPublish> scheduledForRemovalPublishes;
  final Thread cleanThread;

  final int cleanIntervalInMs;
  volatile boolean closed;
  
  public InMemoryIncomingPublishStorage(PublishDataStorage publishDataStorage, int cleanIntervalInMs) {
    this.publishDataStorage = publishDataStorage;
    this.storedPublishes = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.scheduledForRemovalPublishes = DictionaryFactory.stampedLockBasedRefToRefDictionary();
    this.cleanIntervalInMs = cleanIntervalInMs;
    this.cleanThread = new Thread(this::cleanup, "InMemoryIncomingPublishStorage-Cleanup");
    this.cleanThread.setPriority(Thread.MIN_PRIORITY);
    this.cleanThread.setDaemon(true);
    this.cleanThread.start();
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
        throw new IllegalArgumentException("Publish:[%s] already exists".formatted(publishId));
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
    PublishData data = publish.data();
    long stamp = storedPublishes.writeLock();
    try {
      removeWithoutLock(publish.id());
    } finally {
      storedPublishes.writeUnlock(stamp);
    }
    publishDataStorage.removeById(data.id());
  }

  @Override
  public void removeIfExist(IncomingPublish publish) {
    removeIfExist(publish.id());
  }

  private void removeIfExist(UUID publishId) {
    IncomingPublish wasRemoved;
    long stamp = storedPublishes.writeLock();
    try {
      if (storedPublishes.containsKey(publishId)) {
        wasRemoved = removeWithoutLock(publishId);
      } else {
        wasRemoved = null;
      }
    } finally {
      storedPublishes.writeUnlock(stamp);
    }
    if (wasRemoved != null) {
      publishDataStorage.removeById(wasRemoved.data().id());
    }
  }
  
  private IncomingPublish removeWithoutLock(UUID publishId) {
    StoredIncomingPublish stored = storedPublishes.remove(publishId);
    if (stored == null) {
      throw new UnknownPublishStorageException("Unknown publish:[%s]".formatted(publishId), publishId);
    } else if (stored.consumerCount.get() > 0) {
      log.warning("Removed publish:[%s] still has [%s] consumers".formatted(publishId, stored.consumerCount));
    }
    log.debug(publishId, "Removed publish from storage: %s"::formatted);
    return stored.publish();
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
        throw new UnknownPublishStorageException(
            "Unknown publish:[%s]".formatted(publish.id()), 
            publish.id());
      }
    } finally {
      storedPublishes.readUnlock(stamp);
    }
    int result = storedPublish
        .consumerCount()
        .addAndGet(count);
    log.debug(result, publish, "Increased consumers to [%s] for publish: %s"::formatted);
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
        throw new UnknownPublishStorageException(
            "Unknown publish:[%s]".formatted(publish.id()), 
            publish.id());
      }
    } finally {
      storedPublishes.readUnlock(stamp);
    }
    int result = storedPublish
        .consumerCount()
        .accumulateAndGet(count, (current, delta) -> current - delta);
    log.debug(result, publish, "Decreased consumers to [%s] for publish: %s"::formatted);
    if (result == 0) {
      if (!publish.retained()) {
        remove(publish);
      }
    } else if (result < 0) {
      throw new IllegalArgumentException(
          "Unexpected result of decreaseConsumerCount:[%s] for publish:[%s]".formatted(result, publish.id()));
    }
  }

  @Override
  public void scheduleRemoval(IncomingPublish publish, Duration delay) {
    log.debug(publish.id(), delay, "Schedule removal for publish:[%s] with delay:[%s]"::formatted);
    long stamp = scheduledForRemovalPublishes.writeLock();
    try {
      if (scheduledForRemovalPublishes.containsKey(publish.id())) {
        throw new AlreadyScheduledPublishStorageException(
            "Publish:[%s] is already scheduled for removal".formatted(publish.id()));
      }
      var mustBeRemovedAfter = System.currentTimeMillis() + delay.toMillis();
      var scheduled = new ScheduledForRemovalIncomingPublish(publish.id(), mustBeRemovedAfter);
      scheduledForRemovalPublishes.put(publish.id(), scheduled);
    } finally {
      scheduledForRemovalPublishes.writeUnlock(stamp);
    }
  }

  @Override
  public void cancelScheduledRemoval(IncomingPublish publish) {
    log.debug(publish.id(), "Cancel scheduled removal for publish:[%s]"::formatted);
    long stamp = scheduledForRemovalPublishes.writeLock();
    try {
      ScheduledForRemovalIncomingPublish removed = scheduledForRemovalPublishes.remove(publish.id());
      if (removed == null) {
        throw new AlreadyRemovedPublishStorageException(
            "Publish:[%s] is already cancelled".formatted(publish.id()));
      }
    } finally {
      scheduledForRemovalPublishes.writeUnlock(stamp);
    }
  }

  @Override
  public void cancelScheduledRemovalIfExist(IncomingPublish publish) {
    log.debug(publish.id(), "Cancel scheduled removal for publish:[%s] if it exists"::formatted);
    long stamp = scheduledForRemovalPublishes.writeLock();
    try {
      scheduledForRemovalPublishes.remove(publish.id());
    } finally {
      scheduledForRemovalPublishes.writeUnlock(stamp);
    }
  }

  private void cleanup() {
    MutableArray<UUID> localContainer = ArrayFactory.mutableArray(UUID.class, 500);
    var mapOperations = scheduledForRemovalPublishes.operations();
    
    while (!closed) {
      ThreadUtils.sleep(cleanIntervalInMs);
      if (scheduledForRemovalPublishes.isEmpty()) {
        continue;
      }
      localContainer.clear();
      long stamp = scheduledForRemovalPublishes.readLock();
      try {
        long currentInMs = System.currentTimeMillis();
        for (ScheduledForRemovalIncomingPublish scheduled : scheduledForRemovalPublishes) {
          if (scheduled.mustBeRemovedAfterInMs() < currentInMs) {
            localContainer.add(scheduled.publishId());
          }
        }
      } finally {
        scheduledForRemovalPublishes.readUnlock(stamp);
      }
      if (localContainer.isEmpty()) {
        continue;
      }
      for (UUID publishId : localContainer) {
        ScheduledForRemovalIncomingPublish removedFromMap = mapOperations.getInWriteLock(
            publishId,
            MutableRefToRefDictionary::remove);
        if (removedFromMap != null) {
          removeIfExist(publishId);
        }
      }
      localContainer.clear();
    }
  }

  @Override
  public void close() throws IOException {
    closed = true;
  }

  @Getter
  @RequiredArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  private static class StoredIncomingPublish {
    final IncomingPublish publish;
    final AtomicInteger consumerCount = new AtomicInteger(0);
  }

  @Getter
  @RequiredArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  private static class ScheduledForRemovalIncomingPublish {
    final UUID publishId;
    final long mustBeRemovedAfterInMs;
  }
}
