package javasabr.mqtt.model.topic.tree;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.AbstractTrieNode;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.ArrayBuilder;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
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

  private static MutableArray<RetainedMessageNode> childNodesFactory() {
    return ArrayFactory.mutableArray(RetainedMessageNode.class);
  }

  final AtomicReference<@Nullable Publish> retainedMessage = new AtomicReference<>();

  @Override
  protected Supplier<RetainedMessageNode> getNodeFactory() {
    return NODE_FACTORY;
  }

  public void addRetainedMessage(int level, Publish message, TopicName topicName) {
    var child = getOrCreateChildNode(topicName.segment(level));
    boolean isLastLevel = (level + 1 == topicName.levelsCount());
    if (isLastLevel) {
      child.setRetainedMessage(message);
    } else {
      child.addRetainedMessage(level + 1, message, topicName);
    }
  }

  public void removeRetainedMessage(int level, TopicName topicName) {
    var child = getOrCreateChildNode(topicName.segment(level));
    boolean isLastLevel = (level + 1 == topicName.levelsCount());
    if (isLastLevel) {
      child.clearRetainedMessage();
    } else {
      child.removeRetainedMessage(level + 1,
          topicName);
    }
  }

  private void setRetainedMessage(Publish value) {
    retainedMessage.set(value);
  }

  private void clearRetainedMessage() {
    retainedMessage.set(null);
  }

  public void collectRetainedMessages(int level, TopicFilter topicFilter, ArrayBuilder<Publish> result) {
    if (level == topicFilter.levelsCount()) {
      Publish publish = retainedMessage.get();
      if (publish != null) {
        result.add(publish);
      }
      return;
    }
    String segment = topicFilter.segment(level);
    boolean isOneChar = segment.length() == 1;
    if (isOneChar && segment.charAt(0) == TopicFilter.SINGLE_LEVEL_WILDCARD_CHAR) {
      collectAllChildren(level, topicFilter, result);
    } else if (isOneChar && segment.charAt(0) == TopicFilter.MULTI_LEVEL_WILDCARD_CHAR) {
      collectEverything(this, result);
    } else {
      collectExactSegment(level, segment, topicFilter, result);
    }
  }

  private void collectExactSegment(
      int level,
      String segment,
      TopicFilter topicFilter,
      ArrayBuilder<Publish> result) {
    RetainedMessageNode retainedMessageNode = getChildNode(segment);
    if (retainedMessageNode != null) {
      retainedMessageNode.collectRetainedMessages(level + 1, topicFilter, result);
    }
  }

  private void collectAllChildren(int level, TopicFilter topicFilter, ArrayBuilder<Publish> result) {
    var localChildNodes = getChildNodes(RetainedMessageNode::childNodesFactory);
    if (localChildNodes != null) {
      for (RetainedMessageNode childNode : localChildNodes) {
        childNode.collectRetainedMessages(level + 1, topicFilter, result);
      }
    }
  }

  private void collectEverything(RetainedMessageNode node, ArrayBuilder<Publish> result) {
    Publish message = node.retainedMessage.get();
    if (message != null) {
      result.add(message);
    }

    var childNodes = node.getChildNodes(RetainedMessageNode::childNodesFactory);
    if (childNodes != null) {
      for (RetainedMessageNode childNode : childNodes) {
        collectEverything(childNode, result);
      }
    }
  }
}
