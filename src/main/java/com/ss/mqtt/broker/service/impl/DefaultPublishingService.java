package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.handler.publish.in.PublishInHandler;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.PublishingService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultPublishingService implements PublishingService {

  private final PublishInHandler[] publishInHandlers;

  @Override
  public void publish(MqttClient client, PublishInPacket publish) {
    PublishInHandler publishInHandler = publishInHandlers[publish.getQos().ordinal()];
    publishInHandler.handle(client, publish);
  }
}
