package javasabr.mqtt.service.publish.handler;

import java.util.UUID;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.service.PublishDataStorage;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryPublishDataStorage implements PublishDataStorage {
  
  LockableRefToRefDictionary<UUID, PublishData> idToPublishData;

  private InMemoryPublishDataStorage() {
    this.idToPublishData = DictionaryFactory.stampedLockBasedRefToRefDictionary();
  }

  @Override
  public void store(PublishData publishData) {
    long stamp = idToPublishData.writeLock();
    try {
      PublishData exist = idToPublishData.get(publishData.id());
    } finally {
      idToPublishData.writeUnlock(stamp);
    }
  }

  @Override
  public void remove(UUID dataId) {

  }
}
