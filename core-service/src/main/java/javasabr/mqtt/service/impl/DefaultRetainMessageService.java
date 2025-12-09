package javasabr.mqtt.service.impl;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.model.topic.tree.ConcurrentRetainedMessageTree;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.RetainMessageService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultRetainMessageService implements RetainMessageService {

  PublishDeliveringService defaultPublishDeliveringService;
  ConcurrentRetainedMessageTree retainedMessageTree;

  public DefaultRetainMessageService(PublishDeliveringService defaultPublishDeliveringService) {
    this.defaultPublishDeliveringService = defaultPublishDeliveringService;
    this.retainedMessageTree = new ConcurrentRetainedMessageTree();
  }

  @Override
  public void retainMessage(Publish publish) {
    if (publish.retained()) {
      retainedMessageTree.retainMessage(publish);
    }
  }

  @Override
  public Array<PublishHandlingResult> deliverRetainedMessages(SingleSubscriber subscriber) {
    Subscription subscription = subscriber.subscription();
    boolean retainAsPublished = subscription.retainAsPublished();
    Array<Publish> retainedMessages = retainedMessageTree.getRetainedMessage(subscription.topicFilter());
    MutableArray<PublishHandlingResult> result = MutableArray.ofType(PublishHandlingResult.class);
    for (Publish message : retainedMessages) {
      if (!retainAsPublished) {
        message = message.withoutRetain();
      }
      result.add(defaultPublishDeliveringService.startDelivering(message, subscriber));
    }
    return Array.copyOf(result);
  }
}
