package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.DisconnectMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import lombok.CustomLog;

@CustomLog
public class DisconnectMqttInMessageHandler extends AbstractMqttInMessageHandler<ExternalMqttClient, DisconnectMqttInMessage> {

  public DisconnectMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, DisconnectMqttInMessage.class, messageOutFactoryService);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.DISCONNECT;
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      DisconnectMqttInMessage message) {
    DisconnectReasonCode reasonCode = message.reasonCode();
    if (reasonCode == DisconnectReasonCode.NORMAL_DISCONNECTION) {
      log.info(client.clientId(), "Disconnect client:[%s]"::formatted);
    } else {
      log.error("Disconnect client:[%s] by error reason:[%s]".formatted(client, reasonCode));
    }
    connection.close();
  }
}
