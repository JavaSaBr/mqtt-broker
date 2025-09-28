package javasabr.mqtt.network.handler.packet.in;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;

public interface PacketInHandler {

  PacketInHandler EMPTY = (client, packet) -> {};

  void handle(MqttClient.UnsafeMqttClient client, MqttReadablePacket packet);
}
