package javasabr.mqtt.model.topic.tree;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.AbstractTrieNode;
import javasabr.mqtt.model.publish.Publish;
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
class RetainedPublishNode extends AbstractTrieNode<RetainedPublishNode> {

  private final static Supplier<RetainedPublishNode> NODE_FACTORY = RetainedPublishNode::new;

  static {
    DebugUtils.registerIncludedFields("childNodes", "retainedMessage");
  }

  private static MutableArray<RetainedPublishNode> childNodesFactory() {
    return ArrayFactory.mutableArray(RetainedPublishNode.class);
  }

  final AtomicReference<@Nullable Publish> retainedPublishes = new AtomicReference<>();

  @Override
  protected Supplier<RetainedPublishNode> getNodeFactory() {
    return NODE_FACTORY;
  }

  public void addRetainedPublish(int level, Publish publish, TopicName topicName) {
    var child = getOrCreateChildNode(topicName.segment(level));
    int nextLevel = level + 1;
    boolean isLastLevel = (nextLevel == topicName.levelsCount());
    if (isLastLevel) {
      child.setRetainedPublishes(publish);
    } else {
      child.addRetainedPublish(nextLevel, publish, topicName);
    }
  }

  public void removeRetainedPublish(int level, TopicName topicName) {
    var child = getOrCreateChildNode(topicName.segment(level));
    int nextLevel = level + 1;
    boolean isLastLevel = (nextLevel == topicName.levelsCount());
    if (isLastLevel) {
      child.clearRetainedPublish();
    } else {
      child.removeRetainedPublish(nextLevel, topicName);
    }
  }

  private void setRetainedPublishes(Publish value) {
    retainedPublishes.set(value);
  }

  private void clearRetainedPublish() {
    retainedPublishes.set(null);
  }

  public void collectRetainedPublishes(int level, TopicFilter topicFilter, ArrayBuilder<Publish> result) {
    if (level == topicFilter.levelsCount()) {
      Publish publish = retainedPublishes.get();
      if (publish != null) {
        result.add(publish);
      }
    } else if (topicFilter.isSingleLevelWildcard(level)) {
      collectAllChildren(level, topicFilter, result);
    } else if (topicFilter.isMultiLevelWildcard(level)) {
      collectEverything(this, result);
    } else {
      collectExactSegment(level, topicFilter.segment(level), topicFilter, result);
    }
  }

  private void collectExactSegment(
      int level,
      String segment,
      TopicFilter topicFilter,
      ArrayBuilder<Publish> result) {
    RetainedPublishNode retainedPublishNode = getChildNode(segment);
    if (retainedPublishNode != null) {
      retainedPublishNode.collectRetainedPublishes(level + 1, topicFilter, result);
    }
  }

  private void collectAllChildren(int level, TopicFilter topicFilter, ArrayBuilder<Publish> result) {
    var localChildNodes = getChildNodes(RetainedPublishNode::childNodesFactory);
    if (localChildNodes != null) {
      for (RetainedPublishNode childNode : localChildNodes) {
        childNode.collectRetainedPublishes(level + 1, topicFilter, result);
      }
    }
  }

  private void collectEverything(RetainedPublishNode node, ArrayBuilder<Publish> result) {
    Publish publish = node.retainedPublishes.get();
    if (publish != null) {
      result.add(publish);
    }

    var childNodes = node.getChildNodes(RetainedPublishNode::childNodesFactory);
    if (childNodes != null) {
      for (RetainedPublishNode childNode : childNodes) {
        collectEverything(childNode, result);
      }
    }
  }
}
