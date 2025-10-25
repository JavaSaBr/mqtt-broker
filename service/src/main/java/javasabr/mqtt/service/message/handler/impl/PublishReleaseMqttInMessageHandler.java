package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.packet.in.PublishReleaseInPacket;

public class PublishReleaseMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalMqttClient, PublishReleaseInPacket> {

  public PublishReleaseMqttInMessageHandler() {
    super(ExternalMqttClient.class, PublishReleaseInPacket.class);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RELEASED;
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      PublishReleaseInPacket networkPacket) {
    MqttSession session = client.session();
    if (session != null) {
      session.updateInPendingPacket(client, networkPacket);
    }
  }
}
