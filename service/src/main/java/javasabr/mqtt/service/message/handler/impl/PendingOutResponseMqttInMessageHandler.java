package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;

public abstract class PendingOutResponseMqttInMessageHandler<P extends MqttInMessage & TrackableMessage>
    extends AbstractMqttInMessageHandler<ExternalMqttClient, P> {

  protected PendingOutResponseMqttInMessageHandler(
      Class<P> expectedNetworkPacket,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, expectedNetworkPacket, messageOutFactoryService);
  }

  @Override
  protected void processReceived(MqttConnection connection, ExternalMqttClient client, P message) {
    MqttSession session = client.session();
    if (session != null) {
      session.updateOutPendingPacket(client, message);
    }
  }
}
