package javasabr.mqtt.model.topic;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import java.util.Objects;
import java.util.function.Supplier;
import javasabr.mqtt.model.subscriber.SharedSubscriber;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.SubscribeTopicFilter;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.model.utils.SubscriberUtils;
import javasabr.mqtt.model.utils.TopicUtils;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.LockableArray;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.collections.dictionary.Dictionary;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.MutableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopicSubscribers {

  private final static Supplier<TopicSubscribers> TOPIC_SUBSCRIBER_SUPPLIER = TopicSubscribers::new;

  @Getter
  @Nullable
  volatile LockableRefToRefDictionary<String, TopicSubscribers> topicSubscribers;

  @Getter
  @Nullable
  volatile LockableArray<Subscriber> subscribers;

  public void addSubscriber(MqttUser user, SubscribeTopicFilter subscribe) {
    searchPlaceForSubscriber(0, subscribe.getTopicFilter(), user, subscribe);
  }

  private void searchPlaceForSubscriber(
      int level,
      TopicFilter topicFilter,
      MqttUser user,
      SubscribeTopicFilter subscribe) {
    if (level == topicFilter.levelsCount()) {
      LockableArray<Subscriber> subscribers = getOrCreateSubscribers();
      subscribers
          .operations()
          .inWriteLock(user, subscribe, TopicSubscribers::addSubscriber);
    } else {
      LockableRefToRefDictionary<String, TopicSubscribers> topicSubscribers = getOrCreateTopicSubscribers();
      TopicSubscribers topicSubscriber = topicSubscribers
          .operations()
          .getInWriteLock(
              topicFilter.getSegment(level),
              TOPIC_SUBSCRIBER_SUPPLIER,
              MutableRefToRefDictionary::getOrCompute);

      //noinspection ConstantConditions
      topicSubscriber.searchPlaceForSubscriber(level + 1, topicFilter, user, subscribe);
    }
  }

  public void removeSubscriber(MqttUser user, SubscribeTopicFilter subscribe) {
    removeSubscriber(user, subscribe.getTopicFilter());
  }

  public boolean removeSubscriber(MqttUser user, TopicFilter topicFilter) {
    return searchSubscriberToRemove(0, topicFilter, user);
  }

  private boolean searchSubscriberToRemove(int level, TopicFilter topicFilter, MqttUser user) {
    var removed = false;

    LockableRefToRefDictionary<String, TopicSubscribers> topicSubscribers = getTopicSubscribers();
    if (level == topicFilter.levelsCount()) {
      removed = tryToRemoveSubscriber(topicFilter, user);
    } else if (topicSubscribers != null) {
      TopicSubscribers topicSubscriber = topicSubscribers
          .operations()
          .getInReadLock(topicFilter.getSegment(level), Dictionary::get);
      if (topicSubscriber != null) {
        removed = topicSubscriber.searchSubscriberToRemove(level + 1, topicFilter, user);
      }
    }

    return removed;
  }

  private boolean tryToRemoveSubscriber(TopicFilter topicFilter, MqttUser user) {
    LockableArray<Subscriber> subscribers = getSubscribers();
    if (subscribers == null) {
      return false;
    }
    return subscribers
        .operations()
        .getInWriteLock(topicFilter, user, TopicSubscribers::removeSubscriber);
  }

  public Array<SingleSubscriber> matches(TopicName topicName) {
    var resultArray = MutableArray.ofType(SingleSubscriber.class);
    processLevel(0, topicName.getSegment(0), topicName, resultArray);
    return resultArray;
  }

  private void processLevel(int level, String segment, TopicName topicName, MutableArray<SingleSubscriber> result) {
    var nextLevel = level + 1;
    processSegment(nextLevel, segment, topicName, result);
    processSegment(nextLevel, TopicUtils.SINGLE_LEVEL_WILDCARD, topicName, result);
    processSegment(nextLevel, TopicUtils.MULTI_LEVEL_WILDCARD, topicName, result);
  }

  private void processSegment(
      int nextLevel,
      String segment,
      TopicName topicName,
      MutableArray<SingleSubscriber> result) {

    LockableRefToRefDictionary<String, TopicSubscribers> subscribersMap = getTopicSubscribers();
    if (subscribersMap == null) {
      return;
    }

    TopicSubscribers topicSubscribers = subscribersMap
        .operations()
        .getInReadLock(segment, result, TopicSubscribers::collectSubscribers);

    if (topicSubscribers != null && nextLevel < topicName.levelsCount()) {
      String nextSegment = topicName.getSegment(nextLevel);
      topicSubscribers.processLevel(nextLevel, nextSegment, topicName, result);
    }
  }

  private LockableRefToRefDictionary<String, TopicSubscribers> getOrCreateTopicSubscribers() {
    if (topicSubscribers == null) {
      synchronized (this) {
        if (topicSubscribers == null) {
          topicSubscribers = DictionaryFactory.stampedLockBasedRefToRefDictionary();
        }
      }
    }
    //noinspection ConstantConditions
    return topicSubscribers;
  }

  private LockableArray<Subscriber> getOrCreateSubscribers() {
    if (subscribers == null) {
      synchronized (this) {
        if (subscribers == null) {
          subscribers = ArrayFactory.stampedLockBasedArray(Subscriber.class);
        }
      }
    }
    //noinspection ConstantConditions
    return subscribers;
  }

  @Override
  public String toString() {
    return "TopicSubscribers{" + "topicSubscribers=" + topicSubscribers + ", subscribers=" + subscribers + '}';
  }

  private static void addSubscriber(
      LockableArray<Subscriber> subscribers,
      MqttUser user,
      SubscribeTopicFilter subscribe) {
    if (TopicUtils.isShared(subscribe.getTopicFilter())) {
      addSharedSubscriber(subscribers, user, subscribe);
    } else {
      addSingleSubscriber(subscribers, user, subscribe);
    }
  }

  private static void addSingleSubscriber(
      LockableArray<Subscriber> subscribers,
      MqttUser user,
      SubscribeTopicFilter subscribe) {
    subscribers.add(new SingleSubscriber(user, subscribe));
  }

  private static void addSharedSubscriber(
      LockableArray<Subscriber> subscribers,
      MqttUser user,
      SubscribeTopicFilter subscribe) {

    String group = ((SharedTopicFilter) subscribe.getTopicFilter()).getGroup();
    SharedSubscriber sharedSubscriber = (SharedSubscriber) subscribers
        .reversedIterations()
        .findAny(group, SubscriberUtils::isSharedSubscriberWithGroup);

    if (sharedSubscriber == null) {
      sharedSubscriber = new SharedSubscriber(subscribe);
      subscribers.add(sharedSubscriber);
    }

    var singleSubscriber = new SingleSubscriber(user, subscribe);
    sharedSubscriber.addSubscriber(singleSubscriber);
  }

  private static boolean removeSubscriber(LockableArray<Subscriber> subscribers, TopicFilter topic, MqttUser user) {
    return TopicUtils.isShared(topic)
           ? removeSharedSubscriber(subscribers, ((SharedTopicFilter) topic).getGroup(), user)
           : removeSingleSubscriber(subscribers, user);
  }

  private static boolean removeSingleSubscriber(LockableArray<Subscriber> subscribers, MqttUser user) {
    for (int i = 0, length = subscribers.size(); i < length; i++) {
      Subscriber subscriber = subscribers.get(i);
      MqttUser mqttClient = SubscriberUtils.singleSubscriberToMqttUser(subscriber);
      if (Objects.equals(user, mqttClient)) {
        subscribers.remove(i);
        return true;
      }
    }
    return false;
  }

  private static boolean removeSharedSubscriber(
      LockableArray<Subscriber> subscribers,
      String group,
      MqttUser client) {

    boolean removed = false;
    SharedSubscriber sharedSubscriber = (SharedSubscriber) subscribers
        .reversedIterations()
        .findAny(group, SubscriberUtils::isSharedSubscriberWithGroup);

    if (sharedSubscriber != null) {
      removed = sharedSubscriber.removeSubscriber(client);
      if (removed && sharedSubscriber.size() == 0) {
        subscribers.remove(sharedSubscriber);
      }
    }

    return removed;
  }

  private static boolean removeDuplicateWithLowerQoS(MutableArray<SingleSubscriber> result, Subscriber candidate) {
    if (candidate instanceof SharedSubscriber) {
      return true;
    }
    int found = result.indexOf(candidate);
    if (found == -1) {
      return true;
    }
    SingleSubscriber singleSubscriber = (SingleSubscriber) candidate;
    QoS qos = singleSubscriber.getQos();

    SingleSubscriber existed = result.get(found);
    QoS existeQos = existed.getQos();

    if (existeQos.ordinal() < qos.ordinal()) {
      result.remove(found);
      return true;
    } else {
      return false;
    }
  }

  private static void addToResultArray(MutableArray<SingleSubscriber> result, Subscriber subscriber) {
    if (subscriber instanceof SharedSubscriber) {
      result.add(((SharedSubscriber) subscriber).getSubscriber());
    } else {
      result.add((SingleSubscriber) subscriber);
    }
  }

  @Nullable
  private static TopicSubscribers collectSubscribers(
      RefToRefDictionary<String, TopicSubscribers> subscribersMap,
      String segment,
      MutableArray<SingleSubscriber> result) {

    var topicSubscribers = subscribersMap.get(segment);
    if (topicSubscribers == null) {
      return null;
    }
    var subscribers = topicSubscribers.getSubscribers();
    if (subscribers != null) {
      long stamp = subscribers.readLock();
      try {
        for (Subscriber subscriber : subscribers) {
          if (TopicSubscribers.removeDuplicateWithLowerQoS(result, subscriber)) {
            TopicSubscribers.addToResultArray(result, subscriber);
          }
        }
      } finally {
        subscribers.readUnlock(stamp);
      }
    }
    return topicSubscribers;
  }
}
