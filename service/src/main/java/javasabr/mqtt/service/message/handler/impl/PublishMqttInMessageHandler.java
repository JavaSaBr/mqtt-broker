package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.service.PublishReceivingService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishMqttInMessageHandler extends AbstractMqttInMessageHandler<ExternalMqttClient, PublishInPacket> {

  PublishReceivingService publishReceivingService;

  public PublishMqttInMessageHandler(PublishReceivingService publishReceivingService) {
    super(ExternalMqttClient.class, PublishInPacket.class);
    this.publishReceivingService = publishReceivingService;
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      PublishInPacket networkPacket) {
    publishReceivingService.processReceivedPublish(client, networkPacket);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH;
  }
}
