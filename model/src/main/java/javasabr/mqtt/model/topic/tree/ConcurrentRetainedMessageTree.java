package javasabr.mqtt.model.topic.tree;

import java.util.function.Function;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.ThreadSafe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConcurrentRetainedMessageTree implements ThreadSafe {

  RetainedMessageNode rootNode;

  public ConcurrentRetainedMessageTree() {
    this.rootNode = new RetainedMessageNode();
  }

  public void retainMessage(Publish message) {
    rootNode.retainMessage(0, message, message.topicName());
  }

  public Array<Publish> getRetainedMessage(TopicFilter topicFilter) {
    var resultArray = MutableArray.ofType(Publish.class);
    rootNode.collectRetainedMessages(0, topicFilter, resultArray);
    return Array.copyOf(resultArray);
  }
}
