package javasabr.mqtt.service.publish.impl;

import java.util.UUID;
import javasabr.mqtt.model.PayloadFormat;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.model.publish.impl.InMemoryPublishData;
import javasabr.mqtt.service.publish.PublishDataStorage;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryPublishDataStorage implements PublishDataStorage {
  
  LockableRefToRefDictionary<UUID, PublishData> idToPublishData;

  public InMemoryPublishDataStorage() {
    this.idToPublishData = DictionaryFactory.stampedLockBasedRefToRefDictionary();
  }
  
  @Nullable
  @Override
  public PublishData findById(UUID dataId) {
    long stamp = idToPublishData.readLock();
    try {
      return idToPublishData.get(dataId);
    } finally {
      idToPublishData.readUnlock(stamp);
    }
  }
  
  @Override
  public PublishData store(
      UUID dataId,
      @Nullable String contentType,
      PayloadFormat payloadFormat,
      byte[] payload,
      byte @Nullable [] correlationData) {
    long stamp = idToPublishData.writeLock();
    try {
      if (idToPublishData.containsKey(dataId)) {
        throw new IllegalArgumentException("Publish data with id:[%s] already exists".formatted(dataId));
      }
      var publishData = new InMemoryPublishData(
          dataId,
          payloadFormat,
          contentType,
          payload,
          correlationData);
      idToPublishData.put(publishData.id(), publishData);
      return publishData;
    } finally {
      idToPublishData.writeUnlock(stamp);
    }
  }

  @Override
  public void removeById(UUID dataId) {
    long stamp = idToPublishData.writeLock();
    try {
      idToPublishData.remove(dataId);
    } finally {
      idToPublishData.writeUnlock(stamp);
    }
  }
}
