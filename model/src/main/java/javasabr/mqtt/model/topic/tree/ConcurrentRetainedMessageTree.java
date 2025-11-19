package javasabr.mqtt.model.topic.tree;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.rlib.common.ThreadSafe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConcurrentRetainedMessageTree implements ThreadSafe {

  TopicMessageNode rootNode;

  public ConcurrentRetainedMessageTree() {
    this.rootNode = new TopicMessageNode();
  }

  public void retainMessage(Publish message) {
    rootNode.retainMessage(0, message, message.topicName());
  }

  public @Nullable Publish getRetainedMessage(TopicName topicName) {
    return rootNode.getRetainedMessage(0, topicName);
  }

  public @Nullable Publish getRetainedMessage(TopicFilter topicFilter) {
    return rootNode.getRetainedMessage(0, topicFilter);
  }
}
