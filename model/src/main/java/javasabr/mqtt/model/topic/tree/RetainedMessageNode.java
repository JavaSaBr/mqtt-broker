package javasabr.mqtt.model.topic.tree;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.ArrayFactory;
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
    var child = getOrCreateChildNode(topicName.segment(level));
    boolean isLastLevel = (level + 1 == topicName.levelsCount());
    if (isLastLevel) {
      child.retainedMessage.set(message.payload().length == 0 ? null : message);
    } else {
      child.retainMessage(level + 1, message, topicName);
    }
  }

  public void collectRetainedMessages(int level, TopicFilter topicFilter, MutableArray<Publish> result) {
    if (level == topicFilter.levelsCount()) {
      Publish publish = retainedMessage.get();
      if (publish != null) {
        result.add(publish);
      }
      return;
    }
    String segment = topicFilter.segment(level);
    boolean isOneCharSegment = segment.length() == 1;
    if (isOneCharSegment && segment.charAt(0) == TopicFilter.MULTI_LEVEL_WILDCARD_CHAR) {
      collectAllMessages(this, result);
      return;
    }
    if (isOneCharSegment && segment.charAt(0) == TopicFilter.SINGLE_LEVEL_WILDCARD_CHAR) {
      var localChildNodes = childNodes;
      if (localChildNodes != null) {
        var nextChildNodes = ArrayFactory.mutableArray(RetainedMessageNode.class);
        long stamp = localChildNodes.readLock();
        try {
          localChildNodes.values(nextChildNodes);
        } finally {
          localChildNodes.readUnlock(stamp);
        }
        for (RetainedMessageNode childNode : nextChildNodes) {
          childNode.collectRetainedMessages(level + 1, topicFilter, result);
        }
      }
    } else {
      RetainedMessageNode retainedMessageNode = getChildNode(segment);
      if (retainedMessageNode != null) {
        retainedMessageNode.collectRetainedMessages(level + 1, topicFilter, result);
      }
    }
  }

  private void collectAllMessages(RetainedMessageNode node, MutableArray<Publish> result) {
    Queue<RetainedMessageNode> queue = new LinkedList<>();
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
        childNodes.values(queue);
      } finally {
        childNodes.readUnlock(stamp);
      }
    }
  }

  @Nullable
  private RetainedMessageNode getChildNode(String segment) {
    var childNodes = childNodes();
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
    var childNodes = getOrCreateChildNodes();
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
    var current = childNodes;
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

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
