package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage;

public class PublishReleaseMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalMqttClient, PublishReleaseMqttInMessage> {

  public PublishReleaseMqttInMessageHandler() {
    super(ExternalMqttClient.class, PublishReleaseMqttInMessage.class);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RELEASED;
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      PublishReleaseMqttInMessage message) {
    MqttSession session = client.session();
    if (session != null) {
      session.updateInPendingPacket(client, message);
    }
  }
}
