package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.network.session.MqttNetworkSession;
import javasabr.mqtt.service.MessageOutFactoryService;

public abstract class PendingOutResponseMqttInMessageHandler<P extends MqttInMessage & TrackableMqttMessage>
    extends AbstractMqttInMessageHandler<ExternalMqttClient, P> {

  protected PendingOutResponseMqttInMessageHandler(
      Class<P> expectedNetworkPacket,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, expectedNetworkPacket, messageOutFactoryService);
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalMqttClient client,
      MqttNetworkSession session,
      P message) {
    session.updateOutPendingPacket(client, message);
  }
}
