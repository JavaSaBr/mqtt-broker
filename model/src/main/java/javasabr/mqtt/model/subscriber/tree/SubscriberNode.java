package javasabr.mqtt.model.subscriber.tree;

import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.LockableArray;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Getter(AccessLevel.PACKAGE)
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriberNode extends SubscriberTreeBase {

  private final static Supplier<SubscriberNode> SUBSCRIBER_NODE_FACTORY = SubscriberNode::new;

  static {
    DebugUtils.registerIncludedFields("childNodes", "subscribers");
  }

  @Nullable
  volatile LockableRefToRefDictionary<String, SubscriberNode> childNodes;
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
    SubscriberNode childNode = getOrCreateChildNode(topicFilter.segment(level));
    return childNode.subscribe(level + 1, owner, subscription, topicFilter);
  }

  protected boolean unsubscribe(int level, MqttUser owner, TopicFilter topicFilter) {
    if (level == topicFilter.levelsCount()) {
      return removeSubscriber(subscribers(), owner, topicFilter);
    }
    SubscriberNode childNode = getOrCreateChildNode(topicFilter.segment(level));
    return childNode.unsubscribe(level + 1, owner, topicFilter);
  }

  protected void matchesTo(int level, TopicName topicName, int lastLevel, MutableArray<SingleSubscriber> container) {
    collectMatchingSubscribers(topicName.segment(level), level, topicName, lastLevel, container);
    collectMatchingSubscribers(TopicFilter.SINGLE_LEVEL_WILDCARD, level, topicName, lastLevel, container);
    collectMatchingSubscribers(TopicFilter.MULTI_LEVEL_WILDCARD, level, topicName, lastLevel, container);
  }

  private void collectMatchingSubscribers(
      String segment,
      int level,
      TopicName topicName,
      int lastLevel,
      MutableArray<SingleSubscriber> result) {
    SubscriberNode subscriberNode = childNode(segment);
    if (subscriberNode == null) {
      return;
    }
    if (level == lastLevel || TopicFilter.MULTI_LEVEL_WILDCARD.equals(segment)) {
      appendSubscribersTo(result, subscriberNode);
    } else if (level < lastLevel) {
      subscriberNode.matchesTo(level + 1, topicName, lastLevel, result);
    }
  }

  private SubscriberNode getOrCreateChildNode(String segment) {
    LockableRefToRefDictionary<String, SubscriberNode> childNodes = getOrCreateChildNodes();
    long stamp = childNodes.readLock();
    try {
      SubscriberNode subscriberNode = childNodes.get(segment);
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

  @Nullable
  private SubscriberNode childNode(String segment) {
    LockableRefToRefDictionary<String, SubscriberNode> localChildNodes = childNodes;
    if (localChildNodes == null) {
      return null;
    }
    long stamp = localChildNodes.readLock();
    try {
      return localChildNodes.get(segment);
    } finally {
      localChildNodes.readUnlock(stamp);
    }
  }

  private LockableRefToRefDictionary<String, SubscriberNode> getOrCreateChildNodes() {
    LockableRefToRefDictionary<String, SubscriberNode> localChildNodes = childNodes;
    if (localChildNodes != null) {
      return localChildNodes;
    }
    synchronized (this) {
      localChildNodes = childNodes;
      if (localChildNodes == null) {
        localChildNodes = DictionaryFactory.stampedLockBasedRefToRefDictionary();
        childNodes = localChildNodes;
      }
      return localChildNodes;
    }
  }

  private LockableArray<Subscriber> getOrCreateSubscribers() {
    LockableArray<Subscriber> localSubscribers = subscribers;
    if (localSubscribers != null) {
      return localSubscribers;
    }
    synchronized (this) {
      localSubscribers = subscribers;
      if (localSubscribers == null) {
        localSubscribers = ArrayFactory.stampedLockBasedArray(Subscriber.class);
        subscribers = localSubscribers;
      }
      return localSubscribers;
    }
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
