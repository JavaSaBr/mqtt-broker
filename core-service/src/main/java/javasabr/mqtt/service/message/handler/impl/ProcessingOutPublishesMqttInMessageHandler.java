package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publish.OutgoingPublish;
import javasabr.mqtt.model.session.ProcessingPublishes;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;

public abstract class ProcessingOutPublishesMqttInMessageHandler<M extends MqttInMessage & TrackableMqttMessage>
    extends AbstractMqttInMessageHandler<ExternalNetworkMqttUser, M> {

  protected ProcessingOutPublishesMqttInMessageHandler(
      Class<M> expectedNetworkPacket,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, expectedNetworkPacket, messageOutFactoryService);
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      M message) {
    session
        .outgoingProcessingPublishes()
        .apply(user, message);
  }
}
