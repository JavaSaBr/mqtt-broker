package javasabr.mqtt.service.publish.handler;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;

public interface MqttPublishInMessageHandler {

  QoS qos();

  void handle(MqttClient client, PublishInPacket packet);
}
