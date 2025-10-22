package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.network.packet.HasPacketId;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;

public abstract class PendingResponseMqttInMessageHandler<P extends MqttReadablePacket & HasPacketId>
    extends AbstractMqttInMessageHandler<ExternalMqttClient, P> {

  protected PendingResponseMqttInMessageHandler(Class<P> expectedNetworkPacket) {
    super(ExternalMqttClient.class, expectedNetworkPacket);
  }

  @Override
  protected void processReceived(MqttConnection connection, ExternalMqttClient client, P networkPacket) {
    MqttSession session = client.session();
    if (session != null) {
      session.updateOutPendingPacket(client, networkPacket);
    }
  }
}
