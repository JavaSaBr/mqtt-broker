package javasabr.mqtt.service;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscription.Subscription;

public interface PublishDeliveringService {

  void startDelivering(Publish publish, MqttUser user, Subscription subscription);
}
