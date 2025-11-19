package javasabr.mqtt.service.session.impl;

import java.util.concurrent.locks.StampedLock;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.PendingPublishers;
import javasabr.mqtt.model.session.PublishRetryer;
import javasabr.mqtt.model.session.TrackableMessageCallback;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.MutableIntToRefDictionary;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryPendingPublishers implements PendingPublishers {

  record PendingPublish(Publish publish, TrackableMessageCallback callback, PublishRetryer retryer) {}

  MqttSession session;
  MutableIntToRefDictionary<PendingPublish> pending;
  StampedLock lock;

  public InMemoryPendingPublishers(MqttSession session) {
    this.session = session;
    this.pending = DictionaryFactory.mutableIntToRefDictionary();
    this.lock = new StampedLock();
  }

  @Override
  public void register(Publish publish, TrackableMessageCallback callback, PublishRetryer retryer) {
    long stamp = lock.writeLock();
    try {
      PendingPublish exist = pending.get(publish.messageId());
      if (exist != null) {
        throw new IllegalArgumentException("The publish with id:" + publish.messageId() + "is already exist");
      }
      pending.put(publish.messageId(), new PendingPublish(publish, callback, retryer));
    } finally {
      lock.unlockWrite(stamp);
    }
  }
}
