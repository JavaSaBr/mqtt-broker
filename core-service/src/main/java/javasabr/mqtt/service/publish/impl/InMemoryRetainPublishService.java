package javasabr.mqtt.service.publish.impl;

import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.model.topic.tree.ConcurrentRetainedMessageTree;
import javasabr.mqtt.service.publish.RetainPublishService;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InMemoryRetainPublishService implements RetainPublishService {

  ConcurrentRetainedMessageTree retainedMessageTree;

  public InMemoryRetainPublishService() {
    this.retainedMessageTree = new ConcurrentRetainedMessageTree();
  }

  @Nullable
  @Override
  public IncomingPublish retain(IncomingPublish publish) {
    PublishData data = publish.data();
    if (data.isPayloadEmpty()) {
      return retainedMessageTree.removeRetainedMessage(publish.topicName());
    } else {
      return retainedMessageTree.addRetainedMessage(publish);
    }
  }

  @Override
  public Array<IncomingPublish> findRetainedPublishes(TopicFilter topicFilter) {
    return retainedMessageTree.getRetainedMessages(topicFilter);
  }
}
