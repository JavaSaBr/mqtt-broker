package javasabr.mqtt.model.topic.tree;

import static javasabr.mqtt.model.topic.TopicFilter.MULTI_LEVEL_WILDCARD;
import static javasabr.mqtt.model.topic.TopicFilter.SINGLE_LEVEL_WILDCARD;

import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
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
class RetainedMessageNode {

  private final static Supplier<RetainedMessageNode> TOPIC_NODE_FACTORY = RetainedMessageNode::new;

  static {
    DebugUtils.registerIncludedFields("childNodes", "retainedMessage");
  }

  @Nullable
  volatile LockableRefToRefDictionary<String, RetainedMessageNode> childNodes;
  final AtomicReference<@Nullable Publish> retainedMessage = new AtomicReference<>();

  public void retainMessage(int level, Publish message, TopicName topicName) {
    if (level + 1 == topicName.levelsCount()) {
      retainedMessage.set(message.payload().length == 0 ? null : message);
      return;
    }
    RetainedMessageNode childNode = getOrCreateChildNode(topicName.segment(level));
    childNode.retainMessage(level + 1, message, topicName);
  }

  public void collectRetainedMessages(int level, TopicFilter topicFilter, int lastLevel, MutableArray<Publish> result) {
    String segment = topicFilter.segment(level);
    Publish publish = retainedMessage.get();
    if (Objects.equals(segment, MULTI_LEVEL_WILDCARD)) {
      collectAllMessages(this, result);
    } else if (Objects.equals(segment, SINGLE_LEVEL_WILDCARD)) {
      var childNodes = childNodes();
      if (childNodes == null) {
        return;
      }
      long stamp = childNodes.readLock();
      try {
        for (RetainedMessageNode n : childNodes) {
          n.collectRetainedMessages(level + 1, topicFilter, lastLevel, result);
        }
      } finally {
        childNodes.readUnlock(stamp);
      }
    } else if (level == lastLevel && publish != null && Objects.equals(segment, publish.topicName().lastSegment())) {
      result.add(publish);
    } else {
      RetainedMessageNode topicFilterNode = childNode(segment);
      if (topicFilterNode == null) {
        return;
      }
      topicFilterNode.collectRetainedMessages(level + 1, topicFilter, lastLevel, result);
    }
  }

  private void collectAllMessages(RetainedMessageNode node, MutableArray<Publish> result) {
    Queue<RetainedMessageNode> queue = new PriorityQueue<>();
    queue.add(node);
    while (!queue.isEmpty()) {
      RetainedMessageNode poll = queue.poll();
      Publish message = poll.retainedMessage.get();
      if (message != null) {
        result.add(message);
      }
      var childNodes = poll.childNodes();
      if (childNodes == null) {
        continue;
      }
      long stamp = childNodes.readLock();
      try {
        for (RetainedMessageNode n : childNodes) {
          queue.add(n);
        }
      } finally {
        childNodes.readUnlock(stamp);
      }
    }
  }

  @Nullable
  private RetainedMessageNode childNode(String segment) {
    LockableRefToRefDictionary<String, RetainedMessageNode> childNodes = childNodes();
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

  private RetainedMessageNode getOrCreateChildNode(String segment) {
    LockableRefToRefDictionary<String, RetainedMessageNode> childNodes = getOrCreateChildNodes();
    long stamp = childNodes.readLock();
    try {
      RetainedMessageNode topicFilterNode = childNodes.get(segment);
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

  private LockableRefToRefDictionary<String, RetainedMessageNode> getOrCreateChildNodes() {
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
