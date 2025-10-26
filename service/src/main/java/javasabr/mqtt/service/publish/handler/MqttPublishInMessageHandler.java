package javasabr.mqtt.service.publish.handler;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;

public interface MqttPublishInMessageHandler {

  QoS qos();

  void handle(MqttClient client, PublishMqttInMessage packet);
}
