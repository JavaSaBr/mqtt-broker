package javasabr.mqtt.legacy.handler.publish.in;

import javasabr.mqtt.legacy.network.MqttClient;
import javasabr.mqtt.legacy.network.packet.in.PublishInPacket;

/**
 * Interface to handle incoming publish packets.
 */
public interface PublishInHandler {

  void handle(MqttClient client, PublishInPacket packet);
}
