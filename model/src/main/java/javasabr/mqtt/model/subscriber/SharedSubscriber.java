package javasabr.mqtt.model.subscriber;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.SharedTopicFilter;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.LockableArray;
import lombok.AccessLevel;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true, makeFinal = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SharedSubscriber implements Subscriber {

  SharedTopicFilter topicFilter;
  LockableArray<SingleSubscriber> subscribers;
  AtomicInteger current;

  public SharedSubscriber(SharedTopicFilter topicFilter) {
    this.subscribers = ArrayFactory.stampedLockBasedArray(Subscriber.class);
    this.current = new AtomicInteger(0);
    this.topicFilter = topicFilter;
  }

  @Override
  public SingleSubscriber resolveSingle() {
    int nextIndex = current.incrementAndGet();
    long stamp = subscribers.readLock();
    try {
      return next(subscribers, nextIndex);
    } finally {
      subscribers.readUnlock(stamp);
    }
  }

  public void addSubscriber(SingleSubscriber subscriber) {
    subscribers.operations()
        .inWriteLock(subscriber, Collection::add);
  }

  public boolean removeSubscriberWithUser(MqttUser user) {
    if (subscribers.isEmpty()) {
      return false;
    }
    long stamp = subscribers.writeLock();
    try {
      int index = subscribers.indexOf(SingleSubscriber::user, user);
      if (index >= 0) {
        subscribers.remove(index);
        return true;
      }
    } finally {
      subscribers.writeUnlock(stamp);
    }
    return false;
  }

  public int size() {
    //noinspection ConstantConditions
    return subscribers.size();
  }

  public boolean isEmpty() {
    return subscribers.isEmpty();
  }

  public String group() {
    return topicFilter.shareName();
  }

  private static SingleSubscriber next(Array<SingleSubscriber> subscribers, int current) {
    return subscribers.get(current % subscribers.size());
  }
}
