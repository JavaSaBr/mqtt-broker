package javasabr.mqtt.service;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;

public interface PublishingService {

  void publish(MqttClient client, PublishInPacket publish);
}
