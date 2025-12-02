package javasabr.mqtt.model.subscriber.tree;

import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.LockableArray;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import javasabr.rlib.collections.dictionary.MutableRefToRefDictionary;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Getter(AccessLevel.PACKAGE)
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptimizedSubscriberNode extends SubscriberTreeBase {

  private final static Supplier<OptimizedSubscriberNode> SUBSCRIBER_NODE_FACTORY = OptimizedSubscriberNode::new;

  static {
    DebugUtils.registerIncludedFields("childNodes", "subscribers");
  }

  private void appendSubscribersTo(MutableRefToRefDictionary<MqttUser, SingleSubscriber> result, OptimizedSubscriberNode subscriberNode) {
    LockableArray<Subscriber> subscribers = subscriberNode.subscribers();
    if (subscribers == null) {
      return;
    }
    long stamp = subscribers.readLock();
    try {
      for (Subscriber subscriber : subscribers) {
        addOrReplaceIfLowerQos(result, subscriber);
      }
    } finally {
      subscribers.readUnlock(stamp);
    }
  }

  private static void addOrReplaceIfLowerQos(MutableRefToRefDictionary<MqttUser, SingleSubscriber> result, Subscriber subscriber) {
    SingleSubscriber subscriberFromNode = subscriber.resolveSingle();
    SingleSubscriber singleSubscriber = result.get(subscriberFromNode.user());
    if (singleSubscriber == null) {
      result.put(subscriberFromNode.user(), subscriberFromNode);
      return;
    }
    QoS existedQos = singleSubscriber.qos();
    QoS candidateQos = subscriberFromNode.qos();
    if (existedQos.ordinal() < candidateQos.ordinal()) {
      result.put(subscriberFromNode.user(), subscriberFromNode);
    }
  }

  @Nullable
  volatile LockableRefToRefDictionary<String, OptimizedSubscriberNode> childNodes;
  @Nullable
  volatile LockableArray<Subscriber> subscribers;

  /**
   * @return the previous subscription from the same owner
   */
  @Nullable
  protected SingleSubscriber subscribe(int level, MqttUser owner, Subscription subscription, TopicFilter topicFilter) {
    if (level == topicFilter.levelsCount()) {
      return addSubscriber(getOrCreateSubscribers(), owner, subscription, topicFilter);
    }
    OptimizedSubscriberNode childNode = getOrCreateChildNode(topicFilter.segment(level));
    return childNode.subscribe(level + 1, owner, subscription, topicFilter);
  }

  protected boolean unsubscribe(int level, MqttUser owner, TopicFilter topicFilter) {
    if (level == topicFilter.levelsCount()) {
      return removeSubscriber(subscribers(), owner, topicFilter);
    }
    OptimizedSubscriberNode childNode = getOrCreateChildNode(topicFilter.segment(level));
    return childNode.unsubscribe(level + 1, owner, topicFilter);
  }

  protected void matchesTo(int level, TopicName topicName, int lastLevel, MutableRefToRefDictionary<MqttUser, SingleSubscriber> container) {
    LockableRefToRefDictionary<String, OptimizedSubscriberNode> nodes = childNodes();
    if (nodes == null) {
      return;
    }
    collectMatchingSubscribers(topicName.segment(level), level, topicName, lastLevel, container);
    collectMatchingSubscribers(TopicFilter.SINGLE_LEVEL_WILDCARD, level, topicName, lastLevel, container);
    collectMatchingSubscribers(TopicFilter.MULTI_LEVEL_WILDCARD, level, topicName, lastLevel, container);
  }

  private void collectMatchingSubscribers(
      String segment,
      int level,
      TopicName topicName,
      int lastLevel,
      MutableRefToRefDictionary<MqttUser, SingleSubscriber> result) {
    OptimizedSubscriberNode subscriberNode = childNode(segment);
    if (subscriberNode == null) {
      return;
    }
    if (level == lastLevel || TopicFilter.MULTI_LEVEL_WILDCARD.equals(segment)) {
      appendSubscribersTo(result, subscriberNode);
    } else if (level < lastLevel) {
      subscriberNode.matchesTo(level + 1, topicName, lastLevel, result);
    }
  }

  private OptimizedSubscriberNode getOrCreateChildNode(String segment) {
    LockableRefToRefDictionary<String, OptimizedSubscriberNode> childNodes = getOrCreateChildNodes();
    long stamp = childNodes.readLock();
    try {
      OptimizedSubscriberNode subscriberNode = childNodes.get(segment);
      if (subscriberNode != null) {
        return subscriberNode;
      }
    } finally {
      childNodes.readUnlock(stamp);
    }
    stamp = childNodes.writeLock();
    try {
      return childNodes.getOrCompute(segment, SUBSCRIBER_NODE_FACTORY);
    } finally {
      childNodes.writeUnlock(stamp);
    }
  }

  private LockableRefToRefDictionary<String, OptimizedSubscriberNode> getOrCreateChildNodes() {
    LockableRefToRefDictionary<String, OptimizedSubscriberNode> current = childNodes;
    if (current != null) {
      return current;
    }
    synchronized (this) {
      current = childNodes;
      if (current == null) {
        current = DictionaryFactory.stampedLockBasedRefToRefDictionary();
        childNodes = current;
      }
      return current;
    }
  }

  private LockableArray<Subscriber> getOrCreateSubscribers() {
    LockableArray<Subscriber> current = subscribers;
    if (current != null) {
      return current;
    }
    synchronized (this) {
      current = subscribers;
      if (current == null) {
        current = ArrayFactory.stampedLockBasedArray(Subscriber.class);
        subscribers = current;
      }
      return current;
    }
  }

  @Nullable
  private OptimizedSubscriberNode childNode(String segment) {
    LockableRefToRefDictionary<String, OptimizedSubscriberNode> childNodes = childNodes();
    if (childNodes == null) {
      return null;
    }
    long stamp = childNodes.readLock();
    try {
      return childNodes.get(segment);
    } finally {
      childNodes.readUnlock(stamp);
    }
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
