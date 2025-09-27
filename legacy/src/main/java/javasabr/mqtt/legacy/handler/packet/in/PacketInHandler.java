package javasabr.mqtt.legacy.handler.packet.in;

import javasabr.mqtt.legacy.network.client.MqttClient;
import javasabr.mqtt.legacy.network.packet.in.MqttReadablePacket;

public interface PacketInHandler {

  PacketInHandler EMPTY = (client, packet) -> {};

  void handle(MqttClient.UnsafeMqttClient client, MqttReadablePacket packet);
}
