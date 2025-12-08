package javasabr.mqtt.service.session.impl;

import java.util.concurrent.locks.StampedLock;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.model.session.ProcessingPublishes;
import javasabr.mqtt.model.session.PublishRetryer;
import javasabr.mqtt.model.session.TrackableMessageCallback;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.MutableIntToRefDictionary;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryProcessingPublishes implements ProcessingPublishes {

  record InProcessPublish(Publish publish, TrackableMessageCallback callback, PublishRetryer retryer) {}

  MqttSession session;
  MutableIntToRefDictionary<InProcessPublish> processing;
  StampedLock lock;

  public InMemoryProcessingPublishes(NetworkMqttSession session) {
    this.session = session;
    this.processing = DictionaryFactory.mutableIntToRefDictionary();
    this.lock = new StampedLock();
  }

  @Override
  public void register(Publish publish, TrackableMessageCallback callback, PublishRetryer retryer) {
    long stamp = lock.writeLock();
    try {
      InProcessPublish exist = processing.get(publish.messageId());
      if (exist != null) {
        throw new IllegalArgumentException("The publish with id:" + publish.messageId() + "is already exist");
      }
      processing.put(publish.messageId(), new InProcessPublish(publish, callback, retryer));
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Override
  public boolean apply(MqttUser user, TrackableMqttMessage message) {
    long stamp = lock.writeLock();
    try {
      InProcessPublish inProcessPublish = processing.get(message.messageId());
      if (inProcessPublish == null) {
        return false;
      }
      TrackableMessageCallback callback = inProcessPublish.callback();
      boolean shouldBeDeregister = callback.accept(user, session, message);
      if (shouldBeDeregister) {
        processing.remove(message.messageId());
      }
      return true;
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  /**
   * @return the count of resent publishes
   */
  public int resendTo(MqttUser user) {
    int counter = 0;
    long stamp = lock.writeLock();
    try {
      for (InProcessPublish inProcessPublish : processing) {
        PublishRetryer retryer = inProcessPublish.retryer();
        retryer.retry(user, session, inProcessPublish.publish);
        counter++;
      }
    } finally {
      lock.unlockWrite(stamp);
    }
    return counter;
  }
  
  @Override
  public boolean remove(TrackableMqttMessage message) {
    long stamp = lock.writeLock();
    try {
      InProcessPublish inProcessPublish = processing.remove(message.messageId());
      return inProcessPublish != null;
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Override
  public int size() {
    long stamp = lock.readLock();
    try {
      return processing.size();
    } finally {
      lock.unlockRead(stamp);
    }
  }

  public void clear() {
    long stamp = lock.writeLock();
    try {
      processing.clear();
    } finally {
      lock.unlockWrite(stamp);
    }
  }
}
