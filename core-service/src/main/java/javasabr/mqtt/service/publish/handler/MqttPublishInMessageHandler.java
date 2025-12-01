package javasabr.mqtt.service.publish.handler;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface MqttPublishInMessageHandler {

  QoS qos();

  void handle(NetworkMqttUser user, Publish publish);
}
