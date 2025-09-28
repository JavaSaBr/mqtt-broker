package javasabr.mqtt.model.subscriber;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.topic.SharedTopicFilter;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.LockableArray;

public final class SharedSubscriber implements Subscriber {

  private static SingleSubscriber next(Array<SingleSubscriber> subscribers, int current) {
    return subscribers.get(current % subscribers.size());
  }

  private final SharedTopicFilter topicFilter;
  private final LockableArray<SingleSubscriber> subscribers;
  private final AtomicInteger current;

  public SharedSubscriber(SubscribeTopicFilter topic) {
    this.subscribers = ArrayFactory.stampedLockBasedArray(Subscriber.class);
    this.current = new AtomicInteger(0);
    this.topicFilter = (SharedTopicFilter) topic.getTopicFilter();
  }

  public SingleSubscriber getSubscriber() {
    //noinspection ConstantConditions
    return subscribers
        .operations()
        .getInReadLock(current.getAndIncrement(), SharedSubscriber::next);
  }

  public void addSubscriber(SingleSubscriber client) {
    subscribers
        .operations()
        .inWriteLock(client, Collection::add);
  }

  public boolean removeSubscriber(MqttUser user) {
    return subscribers
        .operations()
        .getInWriteLock(
            user, (singleSubscribers, mqttClient) -> {
              int index = singleSubscribers.indexOf(SingleSubscriber::getUser, mqttClient);
              if (index >= 0) {
                singleSubscribers.remove(index);
                return true;
              }
              return false;
            });
  }

  public int size() {
    //noinspection ConstantConditions
    return subscribers.size();
  }

  public String getGroup() {
    return topicFilter.getGroup();
  }
}
