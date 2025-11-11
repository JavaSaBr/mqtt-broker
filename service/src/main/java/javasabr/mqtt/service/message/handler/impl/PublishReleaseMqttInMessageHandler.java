package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;

public class PublishReleaseMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalMqttClient, PublishReleaseMqttInMessage> {

  public PublishReleaseMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, PublishReleaseMqttInMessage.class, messageOutFactoryService);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RELEASED;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalMqttClient client,
      MqttSession session,
      PublishReleaseMqttInMessage message) {
    session.updateInPendingPacket(client, message);
  }
}
