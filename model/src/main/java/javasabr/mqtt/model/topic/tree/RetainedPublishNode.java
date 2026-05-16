package javasabr.mqtt.model.topic.tree;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.AbstractTrieNode;
import javasabr.mqtt.model.publish.IncomingPublish;
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
    DebugUtils.registerIncludedFields("retainedPublish");
  }

  private static MutableArray<RetainedPublishNode> childNodesFactory() {
    return ArrayFactory.mutableArray(RetainedPublishNode.class);
  }

  final AtomicReference<@Nullable IncomingPublish> retainedPublish = new AtomicReference<>();

  @Override
  protected Supplier<RetainedPublishNode> getNodeFactory() {
    return NODE_FACTORY;
  }

  @Nullable
  public IncomingPublish addRetainedPublish(int level, IncomingPublish publish, TopicName topicName) {
    RetainedPublishNode child = getOrCreateChildNode(topicName.segment(level));
    int nextLevel = level + 1;
    boolean isLastLevel = (nextLevel == topicName.levelsCount());
    if (isLastLevel) {
      return child.setRetainedPublish(publish);
    } else {
      return child.addRetainedPublish(nextLevel, publish, topicName);
    }
  }

  @Nullable
  public IncomingPublish removeRetainedPublish(int level, TopicName topicName) {
    RetainedPublishNode child = getChildNode(topicName.segment(level));
    if (child == null) {
      return null;
    }
    int nextLevel = level + 1;
    boolean isLastLevel = (nextLevel == topicName.levelsCount());
    if (isLastLevel) {
      return child.clearRetainedPublish();
    } else {
      return child.removeRetainedPublish(nextLevel, topicName);
    }
  }

  @Nullable
  private IncomingPublish setRetainedPublish(IncomingPublish newPublish) {
    return retainedPublish.getAndSet(newPublish);
  }

  @Nullable
  private IncomingPublish clearRetainedPublish() {
    return retainedPublish.getAndSet(null);
  }

  public void collectRetainedPublishes(int level, TopicFilter topicFilter, ArrayBuilder<IncomingPublish> result) {
    if (level == topicFilter.levelsCount()) {
      IncomingPublish publish = retainedPublish.get();
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
      ArrayBuilder<IncomingPublish> result) {
    RetainedPublishNode retainedPublishNode = getChildNode(segment);
    if (retainedPublishNode != null) {
      retainedPublishNode.collectRetainedPublishes(level + 1, topicFilter, result);
    }
  }

  private void collectAllChildren(int level, TopicFilter topicFilter, ArrayBuilder<IncomingPublish> result) {
    var localChildNodes = getChildNodes(RetainedPublishNode::childNodesFactory);
    if (localChildNodes != null) {
      for (RetainedPublishNode childNode : localChildNodes) {
        childNode.collectRetainedPublishes(level + 1, topicFilter, result);
      }
    }
  }

  private void collectEverything(RetainedPublishNode node, ArrayBuilder<IncomingPublish> result) {
    IncomingPublish publish = node.retainedPublish.get();
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

  @JsonValue
  Object jsonDebugValue() {
    Map<String, Object> result = new HashMap<>(2);
    result.put("childNodes", childNodes);
    result.put("retainedPublish", retainedPublish.get());
    return result;
  }
}
