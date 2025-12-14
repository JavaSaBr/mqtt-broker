package javasabr.mqtt.service.impl;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscription.Subscription;
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
    retainedMessageTree.retainMessage(publish);
  }

  @Override
  public Array<Publish> getRetainedMessages(Subscription subscription) {
    return retainedMessageTree.getRetainedMessage(subscription.topicFilter());
  }
}
