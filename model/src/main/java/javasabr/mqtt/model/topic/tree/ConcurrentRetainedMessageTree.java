package javasabr.mqtt.model.topic.tree;

import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.common.ThreadSafe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConcurrentRetainedMessageTree implements ThreadSafe {

  RetainedPublishNode rootNode;

  public ConcurrentRetainedMessageTree() {
    this.rootNode = new RetainedPublishNode();
  }

  public void addRetainedMessage(Publish publish) {
    rootNode.addRetainedPublish(0, publish, publish.topicName());
  }

  public void removeRetainedMessage(TopicName topicName) {
    rootNode.removeRetainedPublish(0, topicName);
  }

  public Array<Publish> getRetainedMessages(TopicFilter topicFilter) {
    var resultArray = Array.builder(Publish.class);
    rootNode.collectRetainedPublishes(0, topicFilter, resultArray);
    return resultArray.build();
  }
}
