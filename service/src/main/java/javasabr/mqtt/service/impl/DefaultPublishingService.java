package javasabr.mqtt.service.impl;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.handler.publish.PublishInHandler;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.service.PublishingService;
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
