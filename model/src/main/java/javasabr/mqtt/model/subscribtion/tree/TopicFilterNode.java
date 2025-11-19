package javasabr.mqtt.model.subscribtion.tree;

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
class TopicFilterNode extends TopicFilterTreeBase {

  private final static Supplier<TopicFilterNode> TOPIC_NODE_FACTORY = TopicFilterNode::new;

  static {
    DebugUtils.registerIncludedFields("childNodes", "subscribers");
  }

  @Nullable
  volatile LockableRefToRefDictionary<String, TopicFilterNode> childNodes;
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
    TopicFilterNode childNode = getOrCreateChildNode(topicFilter.segment(level));
    return childNode.subscribe(level + 1, owner, subscription, topicFilter);
  }

  public boolean unsubscribe(int level, SubscriptionOwner owner, TopicFilter topicFilter) {
    if (level == topicFilter.levelsCount()) {
      return removeSubscriber(subscribers(), owner, topicFilter);
    }
    TopicFilterNode childNode = getOrCreateChildNode(topicFilter.segment(level));
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
    TopicFilterNode topicFilterNode = childNode(segment);
    if (topicFilterNode == null) {
      return;
    }
    if (level == lastLevel) {
      appendSubscribersTo(result, topicFilterNode);
    } else if (level < lastLevel) {
      topicFilterNode.matchesTo(level + 1, topicName, lastLevel, result);
    }
  }

  private void singleWildcardTopicMatch(
      int level,
      TopicName topicName,
      int lastLevel,
      MutableArray<SingleSubscriber> result) {
    TopicFilterNode topicFilterNode = childNode(TopicFilter.SINGLE_LEVEL_WILDCARD);
    if (topicFilterNode == null) {
      return;
    }
    if (level == lastLevel) {
      appendSubscribersTo(result, topicFilterNode);
    } else if (level < lastLevel) {
      topicFilterNode.matchesTo(level + 1, topicName, lastLevel, result);
    }
  }

  private void multiWildcardTopicMatch(MutableArray<SingleSubscriber> result) {
    TopicFilterNode topicFilterNode = childNode(TopicFilter.MULTI_LEVEL_WILDCARD);
    if (topicFilterNode != null) {
      appendSubscribersTo(result, topicFilterNode);
    }
  }

  private TopicFilterNode getOrCreateChildNode(String segment) {
    LockableRefToRefDictionary<String, TopicFilterNode> childNodes = getOrCreateChildNodes();
    long stamp = childNodes.readLock();
    try {
      TopicFilterNode topicFilterNode = childNodes.get(segment);
      if (topicFilterNode != null) {
        return topicFilterNode;
      }
    } finally {
      childNodes.readUnlock(stamp);
    }
    stamp = childNodes.writeLock();
    try {
      //noinspection DataFlowIssue
      return childNodes.getOrCompute(segment, TOPIC_NODE_FACTORY);
    } finally {
      childNodes.writeUnlock(stamp);
    }
  }

  @Nullable
  private TopicFilterNode childNode(String segment) {
    LockableRefToRefDictionary<String, TopicFilterNode> childNodes = childNodes();
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

  private LockableRefToRefDictionary<String, TopicFilterNode> getOrCreateChildNodes() {
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
