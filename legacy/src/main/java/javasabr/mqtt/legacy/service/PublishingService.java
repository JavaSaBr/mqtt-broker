package javasabr.mqtt.legacy.service;

import javasabr.mqtt.legacy.network.MqttClient;
import javasabr.mqtt.legacy.network.packet.in.PublishInPacket;

public interface PublishingService {

  void publish(MqttClient client, PublishInPacket publish);
}
