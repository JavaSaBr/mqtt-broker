package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;

public interface PacketInHandler {

  PacketInHandler EMPTY = (client, packet) -> {};

  void handle(MqttClient.UnsafeMqttClient client, MqttReadablePacket packet);
}
