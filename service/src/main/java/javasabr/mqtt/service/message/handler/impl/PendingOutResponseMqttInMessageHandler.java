package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.network.packet.HasPacketId;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;

public abstract class PendingOutResponseMqttInMessageHandler<P extends MqttReadablePacket & HasPacketId>
    extends AbstractMqttInMessageHandler<ExternalMqttClient, P> {

  protected PendingOutResponseMqttInMessageHandler(Class<P> expectedNetworkPacket) {
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
