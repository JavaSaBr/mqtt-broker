package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttNetworkSession;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.DisconnectMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import lombok.CustomLog;

@CustomLog
public class DisconnectMqttInMessageHandler extends AbstractMqttInMessageHandler<ExternalNetworkMqttUser, DisconnectMqttInMessage> {

  public DisconnectMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, DisconnectMqttInMessage.class, messageOutFactoryService);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.DISCONNECT;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      MqttNetworkSession session,
      DisconnectMqttInMessage message) {
    DisconnectReasonCode reasonCode = message.reasonCode();
    if (reasonCode == DisconnectReasonCode.NORMAL_DISCONNECTION) {
      log.info(user.clientId(), "Disconnect client:[%s]"::formatted);
    } else {
      log.error("Disconnect client:[%s] by error reason:[%s]".formatted(user, reasonCode));
    }
    connection.close();
  }
}
