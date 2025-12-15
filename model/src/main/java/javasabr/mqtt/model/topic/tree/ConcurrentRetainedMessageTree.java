package javasabr.mqtt.model.topic.tree;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.common.ThreadSafe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConcurrentRetainedMessageTree implements ThreadSafe {

  RetainedMessageNode rootNode;

  public ConcurrentRetainedMessageTree() {
    this.rootNode = new RetainedMessageNode();
  }

  public void addRetainedMessage(Publish message) {
    rootNode.addRetainedMessage(0, message, message.topicName());
  }

  public void removeRetainedMessage(TopicName topicName) {
    rootNode.removeRetainedMessage(0, topicName);
  }

  public Array<Publish> getRetainedMessages(TopicFilter topicFilter) {
    var resultArray = Array.builder(Publish.class);
    rootNode.collectRetainedMessages(0, topicFilter, resultArray);
    return resultArray.build();
  }
}
