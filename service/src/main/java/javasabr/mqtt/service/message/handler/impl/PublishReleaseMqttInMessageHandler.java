package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.PublishReceivingService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishReleaseMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalMqttClient, PublishReleaseMqttInMessage> {

  PublishReceivingService publishReceivingService;

  public PublishReleaseMqttInMessageHandler(
      MessageOutFactoryService messageOutFactoryService,
      PublishReceivingService publishReceivingService) {
    super(ExternalMqttClient.class, PublishReleaseMqttInMessage.class, messageOutFactoryService);
    this.publishReceivingService = publishReceivingService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RELEASE;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalMqttClient client,
      MqttSession session,
      PublishReleaseMqttInMessage message) {
  }
}
