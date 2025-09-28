package javasabr.mqtt.legacy.service.impl;

import javasabr.mqtt.legacy.handler.publish.in.PublishInHandler;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.legacy.service.PublishingService;
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
