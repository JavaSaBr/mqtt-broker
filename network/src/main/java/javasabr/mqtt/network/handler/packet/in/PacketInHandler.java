package javasabr.mqtt.network.handler.packet.in;

import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;

public interface PacketInHandler {

  PacketInHandler EMPTY = (client, packet) -> {};

  void handle(UnsafeMqttClient client, MqttReadablePacket packet);
}
