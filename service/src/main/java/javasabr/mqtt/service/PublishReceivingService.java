package javasabr.mqtt.service;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.network.MqttClient;

public interface PublishReceivingService {

  void processReceivedPublish(MqttClient client, Publish publish);
}
