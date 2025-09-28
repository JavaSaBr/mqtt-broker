package javasabr.mqtt.service.handler.in;

import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.packet.in.DisconnectInPacket;
import lombok.CustomLog;

@CustomLog
public class DisconnetInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, DisconnectInPacket> {

  @Override
  protected void handleImpl(UnsafeMqttClient client, DisconnectInPacket packet) {

    var reasonCode = packet.getReasonCode();

    if (reasonCode == DisconnectReasonCode.NORMAL_DISCONNECTION) {
      log.info(client, "Disconnect client:[%s]"::formatted);
    } else {
      log.error("Disconnect client:[%s] by error reason:[%s]".formatted(client, reasonCode));
    }

    client
        .getConnection()
        .close();
  }
}
