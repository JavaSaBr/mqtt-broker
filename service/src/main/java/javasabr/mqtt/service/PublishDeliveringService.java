package javasabr.mqtt.service;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;

public interface PublishDeliveringService {

  PublishHandlingResult startDelivering(Publish publish, SingleSubscriber subscriber);

  PublishHandlingResult deliverRetainedMessages(TopicFilter topicFilter, SingleSubscriber subscriber);
}
