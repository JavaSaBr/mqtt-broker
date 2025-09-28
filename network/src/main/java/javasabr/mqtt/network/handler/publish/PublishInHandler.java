package javasabr.mqtt.network.handler.publish;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;

/**
 * Interface to handle incoming publish packets.
 */
public interface PublishInHandler {

  void handle(MqttClient client, PublishInPacket packet);
}
