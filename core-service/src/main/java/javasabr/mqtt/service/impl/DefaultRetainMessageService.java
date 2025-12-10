package javasabr.mqtt.service.impl;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
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

  PublishDeliveringService publishDeliveringService;
  ConcurrentRetainedMessageTree retainedMessageTree;

  public DefaultRetainMessageService(PublishDeliveringService publishDeliveringService) {
    this.publishDeliveringService = publishDeliveringService;
    this.retainedMessageTree = new ConcurrentRetainedMessageTree();
  }

  @Override
  public void retainMessage(Publish publish) {
    retainedMessageTree.retainMessage(publish);

  }

  @Override
  public Array<PublishHandlingResult> deliverRetainedMessages(Subscriber subscriber) {
    SingleSubscriber singleSubscriber = subscriber.resolveSingle();
    Subscription subscription = singleSubscriber.subscription();
    boolean retainAsPublished = subscription.retainAsPublished();
    Array<Publish> retainedMessages = retainedMessageTree.getRetainedMessage(subscription.topicFilter());
    MutableArray<PublishHandlingResult> result = MutableArray.ofType(PublishHandlingResult.class);
    for (Publish message : retainedMessages) {
      if (!retainAsPublished) {
        message = message.withoutRetain();
      }
      result.add(publishDeliveringService.startDelivering(message, singleSubscriber));
    }
    return Array.copyOf(result);
  }
}
