package javasabr.mqtt.service;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.mqtt.service.message.out.factory.MqttMessageOutFactory;

public interface MessageOutFactoryService {

  MqttMessageOutFactory resolveFactory(NetworkMqttUser user);

  MqttMessageOutFactory resolveFactory(MqttConnection connection);
}
