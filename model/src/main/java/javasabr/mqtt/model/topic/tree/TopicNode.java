package javasabr.mqtt.model.topic.tree;

import java.util.function.Supplier;
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
class TopicNode extends TopicTreeBase {

  private final static Supplier<TopicNode> TOPIC_NODE_FACTORY = TopicNode::new;

  @Nullable
  volatile LockableRefToRefDictionary<String, TopicNode> childNodes;
  @Nullable
  volatile LockableArray<Subscriber> subscribers;

  public void subscribe(int level, SubscriptionOwner owner, Subscription subscription, TopicFilter topicFilter) {
    if (level == topicFilter.levelsCount()) {
      addSubscriber(getOrCreateSubscribers(), owner, subscription, topicFilter);
      return;
    }
    TopicNode childNode = getOrCreateChildNode(topicFilter.segment(level));
    childNode.subscribe(level + 1, owner, subscription, topicFilter);
  }

  public boolean unsubscribe(int level, SubscriptionOwner owner, TopicFilter topicFilter) {
    if (level == topicFilter.levelsCount()) {
      return removeSubscriber(subscribers(), owner, topicFilter);
    }
    TopicNode childNode = getOrCreateChildNode(topicFilter.segment(level));
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
    TopicNode topicNode = childNode(segment);
    if (topicNode == null) {
      return;
    }
    if (level == lastLevel) {
      appendSubscribersTo(result, topicNode);
    } else if (level < lastLevel) {
      topicNode.matchesTo(level + 1, topicName, lastLevel, result);
    }
  }

  private void singleWildcardTopicMatch(
      int level,
      TopicName topicName,
      int lastLevel,
      MutableArray<SingleSubscriber> result) {
    TopicNode topicNode = childNode(TopicFilter.SINGLE_LEVEL_WILDCARD);
    if (topicNode == null) {
      return;
    }
    if (level == lastLevel) {
      appendSubscribersTo(result, topicNode);
    } else if (level < lastLevel) {
      topicNode.matchesTo(level + 1, topicName, lastLevel, result);
    }
  }

  private void multiWildcardTopicMatch(MutableArray<SingleSubscriber> result) {
    TopicNode topicNode = childNode(TopicFilter.MULTI_LEVEL_WILDCARD);
    if (topicNode != null) {
      appendSubscribersTo(result, topicNode);
    }
  }

  private TopicNode getOrCreateChildNode(String segment) {
    LockableRefToRefDictionary<String, TopicNode> childNodes = getOrCreateChildNodes();
    long stamp = childNodes.readLock();
    try {
      TopicNode topicNode = childNodes.get(segment);
      if (topicNode != null) {
        return topicNode;
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
  private TopicNode childNode(String segment) {
    LockableRefToRefDictionary<String, TopicNode> childNodes = childNodes();
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

  private LockableRefToRefDictionary<String, TopicNode> getOrCreateChildNodes() {
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
}
