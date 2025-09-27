package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;

public interface PublishingService {

  void publish(MqttClient client, PublishInPacket publish);
}
