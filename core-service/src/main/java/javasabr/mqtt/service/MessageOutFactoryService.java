package javasabr.mqtt.service;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.service.message.out.factory.MqttMessageOutFactory;

public interface MessageOutFactoryService {

  MqttMessageOutFactory resolveFactory(MqttClient client);

  MqttMessageOutFactory resolveFactory(MqttConnection connection);
}
