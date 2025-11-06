package javasabr.mqtt.service.publish.handler;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.network.MqttClient;

public interface MqttPublishInMessageHandler {

  QoS qos();

  void handle(MqttClient client, Publish packet);
}
