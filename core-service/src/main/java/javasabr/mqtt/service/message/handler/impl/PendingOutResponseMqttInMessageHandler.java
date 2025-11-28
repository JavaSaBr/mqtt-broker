package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;

public abstract class PendingOutResponseMqttInMessageHandler<P extends MqttInMessage & TrackableMqttMessage>
    extends AbstractMqttInMessageHandler<ExternalNetworkMqttUser, P> {

  protected PendingOutResponseMqttInMessageHandler(
      Class<P> expectedNetworkPacket,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, expectedNetworkPacket, messageOutFactoryService);
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      P message) {
    session.updateOutPendingPacket(user, message);
  }
}
