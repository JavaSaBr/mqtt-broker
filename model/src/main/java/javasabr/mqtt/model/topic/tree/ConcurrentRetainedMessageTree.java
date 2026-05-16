package javasabr.mqtt.model.topic.tree;

import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.common.ThreadSafe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConcurrentRetainedMessageTree implements ThreadSafe {

  RetainedPublishNode rootNode;

  public ConcurrentRetainedMessageTree() {
    this.rootNode = new RetainedPublishNode();
  }

  /**
   * @return the removed prev. retained publish or null.
   */
  @Nullable
  public IncomingPublish addRetainedMessage(IncomingPublish publish) {
    return rootNode.addRetainedPublish(0, publish, publish.topicName());
  }

  /**
   * @return the removed retained publish or null.
   */
  @Nullable
  public IncomingPublish removeRetainedMessage(TopicName topicName) {
    return rootNode.removeRetainedPublish(0, topicName);
  }

  public Array<IncomingPublish> getRetainedMessages(TopicFilter topicFilter) {
    var resultArray = Array.builder(IncomingPublish.class);
    rootNode.collectRetainedPublishes(0, topicFilter, resultArray);
    return resultArray.build();
  }
}
