package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishReceivingService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishMqttInMessageHandler extends AbstractMqttInMessageHandler<ExternalMqttClient, PublishMqttInMessage> {

  PublishReceivingService publishReceivingService;

  public PublishMqttInMessageHandler(
      PublishReceivingService publishReceivingService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, PublishMqttInMessage.class, messageOutFactoryService);
    this.publishReceivingService = publishReceivingService;
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      PublishMqttInMessage message) {
    publishReceivingService.processReceivedPublish(client, message);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH;
  }
}
