package javasabr.mqtt.service;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;

public interface PublishReceivingService {

  void processReceivedPublish(MqttClient client, PublishInPacket publish);
}
