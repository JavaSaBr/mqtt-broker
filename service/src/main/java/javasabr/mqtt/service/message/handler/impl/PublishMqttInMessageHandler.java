package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.network.packet.MqttPacketType;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.service.PublishingService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishMqttInMessageHandler extends AbstractMqttInMessageHandler<ExternalMqttClient, PublishInPacket> {

  PublishingService publishingService;

  public PublishMqttInMessageHandler(PublishingService publishingService) {
    super(ExternalMqttClient.class, PublishInPacket.class);
    this.publishingService = publishingService;
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      PublishInPacket networkPacket) {
    publishingService.publish(client, networkPacket);
  }

  @Override
  public MqttPacketType messageType() {
    return MqttPacketType.PUBLISH;
  }
}
