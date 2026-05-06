package javasabr.mqtt.service.publish.impl;

import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.tree.ConcurrentRetainedMessageTree;
import javasabr.mqtt.service.publish.RetainPublishService;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryRetainPublishService implements RetainPublishService {

  ConcurrentRetainedMessageTree retainedMessageTree;

  public InMemoryRetainPublishService() {
    this.retainedMessageTree = new ConcurrentRetainedMessageTree();
  }

  @Override
  public void retain(Publish publish) {
    PublishData data = publish.data();
    if (data.isPayloadEmpty()) {
      retainedMessageTree.removeRetainedMessage(publish.topicName());
    } else {
      retainedMessageTree.addRetainedMessage(publish);
    }
  }

  @Override
  public Array<Publish> findRetainedPublishes(TopicFilter topicFilter) {
    return retainedMessageTree.getRetainedMessages(topicFilter);
  }
}
