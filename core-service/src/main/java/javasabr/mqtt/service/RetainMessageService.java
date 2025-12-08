package javasabr.mqtt.service;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import javasabr.rlib.collections.array.Array;

public interface RetainMessageService {

  void retainMessage(Publish publish, Subscription subscription);

  Array<PublishHandlingResult> deliverRetainedMessages(SingleSubscriber subscriber);
}
