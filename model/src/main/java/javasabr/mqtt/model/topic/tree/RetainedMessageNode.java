package javasabr.mqtt.model.topic.tree;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.AbstractTrieNode;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.collections.deque.DequeFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Getter(AccessLevel.PACKAGE)
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
class RetainedMessageNode extends AbstractTrieNode<RetainedMessageNode> {

  private final static Supplier<RetainedMessageNode> NODE_FACTORY = RetainedMessageNode::new;

  static {
    DebugUtils.registerIncludedFields("childNodes", "retainedMessage");
  }

  final AtomicReference<@Nullable Publish> retainedMessage = new AtomicReference<>();

  @Override
  protected Supplier<RetainedMessageNode> getNodeFactory() {
    return NODE_FACTORY;
  }

  public void retainMessage(int level, Publish message, TopicName topicName) {
    var child = getOrCreateChildNode(topicName.segment(level));
    boolean isLastLevel = (level + 1 == topicName.levelsCount());
    if (isLastLevel) {
      child.retainedMessage.set(message.payload().length == 0 ? null : message);
    } else {
      child.retainMessage(level + 1, message, topicName);
    }
  }

  public void collectRetainedMessages(int level, TopicFilter topicFilter, MutableArray<Publish> result,
                                      Function<Publish, Publish> publishTransformer) {
    if (level == topicFilter.levelsCount()) {
      Publish publish = retainedMessage.get();
      if (publish != null) {
        result.add(publishTransformer.apply(publish));
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
      var localChildNodes = getChildNodes(() -> ArrayFactory.mutableArray(RetainedMessageNode.class));
      if (localChildNodes != null) {
        for (RetainedMessageNode childNode : localChildNodes) {
          childNode.collectRetainedMessages(level + 1, topicFilter, result, publishTransformer);
        }
      }
    } else {
      RetainedMessageNode retainedMessageNode = getChildNode(segment);
      if (retainedMessageNode != null) {
        retainedMessageNode.collectRetainedMessages(level + 1, topicFilter, result, publishTransformer);
      }
    }
  }

  private void collectAllMessages(RetainedMessageNode node, MutableArray<Publish> result) {
    Queue<RetainedMessageNode> queue = DequeFactory.arrayBasedBased(RetainedMessageNode.class);
    queue.add(node);
    while (!queue.isEmpty()) {
      RetainedMessageNode poll = queue.poll();
      Publish message = poll.retainedMessage.get();
      if (message != null) {
        result.add(message);
      }
      poll.collectChildNodes(queue);
    }
  }
}
