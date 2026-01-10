package javasabr.mqtt.service.session.impl;

import java.util.concurrent.locks.StampedLock;
import javasabr.mqtt.model.session.ActiveSubscriptions;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryActiveSubscriptions implements ActiveSubscriptions {

  private static final Array<Subscription> EMPTY_SUBSCRIPTIONS = Array.empty(Subscription.class);

  MutableArray<Subscription> subscriptions;

  StampedLock lock;

  public InMemoryActiveSubscriptions() {
    this.subscriptions = MutableArray.ofType(Subscription.class);
    this.lock = new StampedLock();
  }

  @Override
  public void add(Subscription subscription) {
    long stamp = lock.writeLock();
    try {
      subscriptions.add(subscription);
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Override
  public void remove(Subscription subscription) {
    long stamp = lock.writeLock();
    try {
      subscriptions.remove(subscription);
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Override
  public void removeByTopicFilter(TopicFilter topicFilter) {
    long stamp = lock.writeLock();
    try {
      int index = subscriptions.indexOf(Subscription::topicFilter, topicFilter);
      if (index >= 0) {
        subscriptions.remove(index);
      }
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Override
  public Array<Subscription> subscriptions() {
    long stamp = lock.readLock();
    try {
      if (subscriptions.isEmpty()) {
        return EMPTY_SUBSCRIPTIONS;
      }
      return Array.copyOf(subscriptions);
    } finally {
      lock.unlockRead(stamp);
    }
  }

  @Override
  public Array<Subscription> findBySubscriptionId(int subscriptionId) {
    MutableArray<Subscription> result = ArrayFactory.mutableArray(Subscription.class);
    long stamp = lock.readLock();
    try {
      for (Subscription subscription : subscriptions) {
        if (subscription.subscriptionId() == subscriptionId) {
          result.add(subscription);
        }
      }
    } finally {
      lock.unlockRead(stamp);
    }
    return result;
  }

  public void clear() {
    long stamp = lock.writeLock();
    try {
      subscriptions.clear();
    } finally {
      lock.unlockWrite(stamp);
    }
  }
  
  @Override
  public boolean isEmpty() {
    long stamp = lock.readLock();
    try {
      return subscriptions.isEmpty();
    } finally {
      lock.unlockRead(stamp);
    }
  }
}
