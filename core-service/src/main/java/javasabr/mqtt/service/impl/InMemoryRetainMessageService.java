package javasabr.mqtt.service.impl;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.tree.ConcurrentRetainedMessageTree;
import javasabr.mqtt.service.RetainMessageService;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryRetainMessageService implements RetainMessageService {

  ConcurrentRetainedMessageTree retainedMessageTree;

  public InMemoryRetainMessageService() {
    this.retainedMessageTree = new ConcurrentRetainedMessageTree();
  }

  @Override
  public void retainMessage(Publish publish) {
    if (publish.payload().length == 0) {
      retainedMessageTree.removeRetainedMessage(publish.topicName());
    } else {
      retainedMessageTree.addRetainedMessage(publish);
    }
  }

  @Override
  public Array<Publish> getRetainedMessages(TopicFilter topicFilter) {
    return retainedMessageTree.getRetainedMessages(topicFilter);
  }
}
