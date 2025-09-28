package javasabr.mqtt.model.topic;

import javasabr.mqtt.model.QoS;
import java.util.Objects;
import java.util.function.Supplier;
import javasabr.mqtt.model.network.MqttClient;
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
import lombok.Getter;
import org.jspecify.annotations.Nullable;

public class TopicSubscribers {

  private final static Supplier<TopicSubscribers> TOPIC_SUBSCRIBER_SUPPLIER = TopicSubscribers::new;

  private static void addSubscriber(
      LockableArray<Subscriber> subscribers,
      MqttClient client,
      SubscribeTopicFilter subscribe) {
    if (TopicUtils.isShared(subscribe.getTopicFilter())) {
      addSharedSubscriber(subscribers, client, subscribe);
    } else {
      addSingleSubscriber(subscribers, client, subscribe);
    }
  }

  private static void addSingleSubscriber(
      LockableArray<Subscriber> subscribers,
      MqttClient client,
      SubscribeTopicFilter subscribe) {
    subscribers.add(new SingleSubscriber(client, subscribe));
  }

  private static void addSharedSubscriber(
      LockableArray<Subscriber> subscribers,
      MqttClient client,
      SubscribeTopicFilter subscribe) {

    String group = ((SharedTopicFilter) subscribe.getTopicFilter()).getGroup();
    SharedSubscriber sharedSubscriber = (SharedSubscriber) subscribers
        .reversedIterations()
        .findAny(group, SubscriberUtils::isSharedSubscriberWithGroup);

    if (sharedSubscriber == null) {
      sharedSubscriber = new SharedSubscriber(subscribe);
      subscribers.add(sharedSubscriber);
    }

    var singleSubscriber = new SingleSubscriber(client, subscribe);
    sharedSubscriber.addSubscriber(singleSubscriber);
  }

  private static boolean removeSubscriber(LockableArray<Subscriber> subscribers, TopicFilter topic, MqttClient client) {
    return TopicUtils.isShared(topic)
           ? removeSharedSubscriber(subscribers, ((SharedTopicFilter) topic).getGroup(), client)
           : removeSingleSubscriber(subscribers, client);
  }

  private static boolean removeSingleSubscriber(LockableArray<Subscriber> subscribers, MqttClient client) {
    for (int i = 0, length = subscribers.size(); i < length; i++) {
      Subscriber subscriber = subscribers.get(i);
      MqttClient mqttClient = SubscriberUtils.singleSubscriberToMqttClient(subscriber);
      if (Objects.equals(client, mqttClient)) {
        subscribers.remove(i);
        return true;
      }
    }
    return false;
  }

  private static boolean removeSharedSubscriber(
      LockableArray<Subscriber> subscribers,
      String group,
      MqttClient client) {

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

  private volatile @Getter
  @Nullable LockableRefToRefDictionary<String, TopicSubscribers> topicSubscribers;

  private volatile @Getter
  @Nullable LockableArray<Subscriber> subscribers;

  public void addSubscriber(MqttClient client, SubscribeTopicFilter subscribe) {
    searchPlaceForSubscriber(0, subscribe.getTopicFilter(), client, subscribe);
  }

  private void searchPlaceForSubscriber(
      int level,
      TopicFilter topicFilter,
      MqttClient client,
      SubscribeTopicFilter subscribe) {
    if (level == topicFilter.levelsCount()) {
      LockableArray<Subscriber> subscribers = getOrCreateSubscribers();
      subscribers
          .operations()
          .inWriteLock(client, subscribe, TopicSubscribers::addSubscriber);
    } else {
      LockableRefToRefDictionary<String, TopicSubscribers> topicSubscribers = getOrCreateTopicSubscribers();
      TopicSubscribers topicSubscriber = topicSubscribers
          .operations()
          .getInWriteLock(
              topicFilter.getSegment(level),
              TOPIC_SUBSCRIBER_SUPPLIER,
              MutableRefToRefDictionary::getOrCompute);

      //noinspection ConstantConditions
      topicSubscriber.searchPlaceForSubscriber(level + 1, topicFilter, client, subscribe);
    }
  }

  public void removeSubscriber(MqttClient client, SubscribeTopicFilter subscribe) {
    removeSubscriber(client, subscribe.getTopicFilter());
  }

  public boolean removeSubscriber(MqttClient client, TopicFilter topicFilter) {
    return searchSubscriberToRemove(0, topicFilter, client);
  }

  private boolean searchSubscriberToRemove(int level, TopicFilter topicFilter, MqttClient mqttClient) {
    var removed = false;

    LockableRefToRefDictionary<String, TopicSubscribers> topicSubscribers = getTopicSubscribers();
    if (level == topicFilter.levelsCount()) {
      removed = tryToRemoveSubscriber(topicFilter, mqttClient);
    } else if (topicSubscribers != null) {
      TopicSubscribers topicSubscriber = topicSubscribers
          .operations()
          .getInReadLock(topicFilter.getSegment(level), Dictionary::get);
      if (topicSubscriber != null) {
        removed = topicSubscriber.searchSubscriberToRemove(level + 1, topicFilter, mqttClient);
      }
    }

    return removed;
  }

  private boolean tryToRemoveSubscriber(TopicFilter topicFilter, MqttClient mqttClient) {
    LockableArray<Subscriber> subscribers = getSubscribers();
    if (subscribers == null) {
      return false;
    }
    return subscribers
        .operations()
        .getInWriteLock(topicFilter, mqttClient, TopicSubscribers::removeSubscriber);
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
}
