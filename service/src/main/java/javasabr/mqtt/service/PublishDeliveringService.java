package javasabr.mqtt.service;

import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;

public interface PublishDeliveringService {

  PublishHandlingResult startDelivering(PublishMqttInMessage publish, SingleSubscriber subscriber);
}
