package javasabr.mqtt.service;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;

public interface PublishReceivingService {

  void processReceivedPublish(MqttClient client, PublishMqttInMessage publish);
}
