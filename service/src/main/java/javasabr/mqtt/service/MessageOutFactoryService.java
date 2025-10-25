package javasabr.mqtt.service;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.service.message.out.factory.MessageOutFactory;

public interface MessageOutFactoryService {

  MessageOutFactory resolveFactory(MqttClient client);

  MessageOutFactory resolveFactory(MqttConnection connection);
}
