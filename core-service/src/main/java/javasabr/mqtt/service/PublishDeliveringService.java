package javasabr.mqtt.service;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;

public interface PublishDeliveringService {

  void startDelivering(Publish publish, SingleSubscriber subscriber);
}
