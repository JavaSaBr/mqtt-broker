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
  public void store(PublishData publishData) {
    long stamp = idToPublishData.writeLock();
    try {
      PublishData exist = idToPublishData.putIfAbsent(publishData.id(), publishData);
      if (exist != null) {
        throw new IllegalArgumentException("Publish data:%s already exists".formatted(publishData));
      }
    } finally {
      idToPublishData.writeUnlock(stamp);
    }
  }

  @Override
  public PublishData store(
      UUID dataId,
      @Nullable String contentType,
      PayloadFormat payloadFormat,
      byte[] payload,
      byte @Nullable [] correlationData) {
    var publishData = new InMemoryPublishData(
        dataId, 
        payloadFormat,
        contentType,
        payload, 
        correlationData);
    store(publishData);
    return publishData;
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
