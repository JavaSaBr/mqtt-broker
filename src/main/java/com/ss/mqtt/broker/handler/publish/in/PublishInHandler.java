package com.ss.mqtt.broker.handler.publish.in;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;

/**
 * Interface to handle incoming publish packets.
 */
public interface PublishInHandler {

  void handle(MqttClient client, PublishInPacket packet);
}
