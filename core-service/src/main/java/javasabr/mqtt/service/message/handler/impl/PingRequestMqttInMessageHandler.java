package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.PingRequestMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;

public class PingRequestMqttInMessageHandler extends
    AbstractMqttInMessageHandler<ExternalNetworkMqttUser, PingRequestMqttInMessage> {

  public PingRequestMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, PingRequestMqttInMessage.class, messageOutFactoryService);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PING_REQUEST;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      PingRequestMqttInMessage message) {
    MqttOutMessage pingResponse = messageOutFactoryService
        .resolveFactory(user)
        .newPingResponse();
    user.sendInBackground(pingResponse);
  }
}
