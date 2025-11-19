package javasabr.mqtt.model.subscriber.tree;

import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.model.subscribtion.SubscriptionOwner;
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
class SubscriberNode extends SubscriberTreeBase {

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
  public SingleSubscriber subscribe(int level, SubscriptionOwner owner, Subscription subscription, TopicFilter topicFilter) {
    if (level == topicFilter.levelsCount()) {
      return addSubscriber(getOrCreateSubscribers(), owner, subscription, topicFilter);
    }
    SubscriberNode childNode = getOrCreateChildNode(topicFilter.segment(level));
    return childNode.subscribe(level + 1, owner, subscription, topicFilter);
  }

  public boolean unsubscribe(int level, SubscriptionOwner owner, TopicFilter topicFilter) {
    if (level == topicFilter.levelsCount()) {
      return removeSubscriber(subscribers(), owner, topicFilter);
    }
    SubscriberNode childNode = getOrCreateChildNode(topicFilter.segment(level));
    return childNode.unsubscribe(level + 1, owner, topicFilter);
  }

  protected void matchesTo(int level, TopicName topicName, int lastLevel, MutableArray<SingleSubscriber> container) {
    exactlyTopicMatch(level, topicName, lastLevel, container);
    singleWildcardTopicMatch(level, topicName, lastLevel, container);
    multiWildcardTopicMatch(container);
  }

  private void exactlyTopicMatch(
      int level,
      TopicName topicName,
      int lastLevel,
      MutableArray<SingleSubscriber> result) {
    String segment = topicName.segment(level);
    SubscriberNode subscriberNode = childNode(segment);
    if (subscriberNode == null) {
      return;
    }
    if (level == lastLevel) {
      appendSubscribersTo(result, subscriberNode);
    } else if (level < lastLevel) {
      subscriberNode.matchesTo(level + 1, topicName, lastLevel, result);
    }
  }

  private void singleWildcardTopicMatch(
      int level,
      TopicName topicName,
      int lastLevel,
      MutableArray<SingleSubscriber> result) {
    SubscriberNode subscriberNode = childNode(TopicFilter.SINGLE_LEVEL_WILDCARD);
    if (subscriberNode == null) {
      return;
    }
    if (level == lastLevel) {
      appendSubscribersTo(result, subscriberNode);
    } else if (level < lastLevel) {
      subscriberNode.matchesTo(level + 1, topicName, lastLevel, result);
    }
  }

  private void multiWildcardTopicMatch(MutableArray<SingleSubscriber> result) {
    SubscriberNode subscriberNode = childNode(TopicFilter.MULTI_LEVEL_WILDCARD);
    if (subscriberNode != null) {
      appendSubscribersTo(result, subscriberNode);
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
      //noinspection DataFlowIssue
      return childNodes.getOrCompute(segment, SUBSCRIBER_NODE_FACTORY);
    } finally {
      childNodes.writeUnlock(stamp);
    }
  }

  @Nullable
  private SubscriberNode childNode(String segment) {
    LockableRefToRefDictionary<String, SubscriberNode> childNodes = childNodes();
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

  private LockableRefToRefDictionary<String, SubscriberNode> getOrCreateChildNodes() {
    if (childNodes == null) {
      synchronized (this) {
        if (childNodes == null) {
          childNodes = DictionaryFactory.stampedLockBasedRefToRefDictionary();
        }
      }
    }
    //noinspection ConstantConditions
    return childNodes;
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
    return DebugUtils.toJsonString(this);
  }
}
