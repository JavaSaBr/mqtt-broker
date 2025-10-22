package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.network.packet.MqttPacketType;
import javasabr.mqtt.network.packet.in.DisconnectInPacket;
import lombok.CustomLog;

@CustomLog
public class DisconnectMqttInMessageHandler extends AbstractMqttInMessageHandler<ExternalMqttClient, DisconnectInPacket> {

  public DisconnectMqttInMessageHandler() {
    super(ExternalMqttClient.class, DisconnectInPacket.class);
  }

  @Override
  public int packetType() {
    return MqttPacketType.DISCONNECT.typeIndex();
  }

  @Override
  protected void processReceived(
      MqttConnection connection,
      ExternalMqttClient client,
      DisconnectInPacket networkPacket) {
    DisconnectReasonCode reasonCode = networkPacket.getReasonCode();
    if (reasonCode == DisconnectReasonCode.NORMAL_DISCONNECTION) {
      log.info(client, "Disconnect client:[%s]"::formatted);
    } else {
      log.error("Disconnect client:[%s] by error reason:[%s]".formatted(client, reasonCode));
    }
    connection.close();
  }
}
