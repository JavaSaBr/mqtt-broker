package javasabr.mqtt.service.session.impl;

import java.util.concurrent.locks.StampedLock;
import javasabr.mqtt.model.session.TopicNameMapping;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.MutableIntToRefDictionary;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryTopicNameMapping implements TopicNameMapping {

  MutableIntToRefDictionary<TopicName> topicNameAliases;
  StampedLock lock;

  public InMemoryTopicNameMapping() {
    this.topicNameAliases = DictionaryFactory.mutableIntToRefDictionary();
    this.lock = new StampedLock();
  }

  @Override
  public void update(int topicAlias, TopicName topicName) {
    long stamp = lock.readLock();
    try {
      TopicName existed = topicNameAliases.get(topicAlias);
      if (existed != null && existed.equals(topicName)) {
        return;
      }
    } finally {
      lock.unlockRead(stamp);
    }
    stamp = lock.writeLock();
    try {
      topicNameAliases.put(topicAlias, topicName);
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Nullable
  @Override
  public TopicName resolve(int topicAlias) {
    long stamp = lock.readLock();
    try {
      return topicNameAliases.get(topicAlias);
    } finally {
      lock.unlockRead(stamp);
    }
  }

  @Override
  public int size() {
    long stamp = lock.readLock();
    try {
      return topicNameAliases.size();
    } finally {
      lock.unlockRead(stamp);
    }
  }

  public void clear() {
    long stamp = lock.writeLock();
    try {
      topicNameAliases.clear();
    } finally {
      lock.unlockWrite(stamp);
    }
  }
}
