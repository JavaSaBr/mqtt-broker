package javasabr.mqtt.service;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import javasabr.rlib.collections.array.Array;

public interface PublishDeliveringService {

  PublishHandlingResult startDelivering(Publish publish, SingleSubscriber subscriber);

  Array<PublishHandlingResult> deliverRetainedMessages(TopicFilter topicFilter, SingleSubscriber subscriber);
}
