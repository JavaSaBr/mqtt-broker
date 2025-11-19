package javasabr.mqtt.model.topic.tree;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
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
class TopicMessageNode {

  private final static Supplier<TopicMessageNode> TOPIC_NODE_FACTORY = TopicMessageNode::new;

  static {
    DebugUtils.registerIncludedFields("childNodes", "retainedMessage");
  }

  @Nullable
  volatile LockableRefToRefDictionary<String, TopicMessageNode> childNodes;
  final AtomicReference<@Nullable Publish> retainedMessage = new AtomicReference<>();

  public void retainMessage(int level, Publish message, TopicName topicFilter) {
    if (level + 1 == topicFilter.levelsCount()) {
      retainedMessage.set(message);
      return;
    }
    TopicMessageNode childNode = getOrCreateChildNode(topicFilter.segment(level));
    childNode.retainMessage(level + 1, message, topicFilter);
  }

  @Nullable
  public Publish getRetainedMessage(int level, TopicName topicName) {
    if (level + 1 == topicName.levelsCount()) {
      return retainedMessage.get();
    }
    TopicMessageNode childNode = getOrCreateChildNode(topicName.segment(level));
    return childNode.getRetainedMessage(level + 1, topicName);
  }

  @Nullable
  public Publish getRetainedMessage(int level, TopicFilter topicName) {
    if (level + 1 == topicName.levelsCount()) {
      return retainedMessage.get();
    }
    TopicMessageNode childNode = getOrCreateChildNode(topicName.segment(level));
    return childNode.getRetainedMessage(level + 1, topicName);
  }

  private TopicMessageNode getOrCreateChildNode(String segment) {
    LockableRefToRefDictionary<String, TopicMessageNode> childNodes = getOrCreateChildNodes();
    long stamp = childNodes.readLock();
    try {
      TopicMessageNode topicFilterNode = childNodes.get(segment);
      if (topicFilterNode != null) {
        return topicFilterNode;
      }
    } finally {
      childNodes.readUnlock(stamp);
    }
    stamp = childNodes.writeLock();
    try {
      return childNodes.getOrCompute(segment, TOPIC_NODE_FACTORY);
    } finally {
      childNodes.writeUnlock(stamp);
    }
  }

  private LockableRefToRefDictionary<String, TopicMessageNode> getOrCreateChildNodes() {
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

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
