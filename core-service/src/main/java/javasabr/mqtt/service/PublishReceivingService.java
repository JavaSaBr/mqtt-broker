package javasabr.mqtt.service;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface PublishReceivingService {

  void processPublish(NetworkMqttUser user, Publish publish);
}
