package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.HasMessageId;
import javasabr.mqtt.network.message.in.MqttInMessage;

public abstract class PendingOutResponseMqttInMessageHandler<P extends MqttInMessage & HasMessageId>
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
