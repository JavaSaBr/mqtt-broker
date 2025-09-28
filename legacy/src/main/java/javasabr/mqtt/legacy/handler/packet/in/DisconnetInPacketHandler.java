package javasabr.mqtt.legacy.handler.packet.in;

import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.legacy.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.legacy.network.packet.in.DisconnectInPacket;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DisconnetInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, DisconnectInPacket> {

  @Override
  protected void handleImpl(UnsafeMqttClient client, DisconnectInPacket packet) {

    var reasonCode = packet.getReasonCode();

    if (reasonCode == DisconnectReasonCode.NORMAL_DISCONNECTION) {
      log.info("Disconnect client {}", client);
    } else {
      log.error("Disconnect client {} by error reason {}", client, reasonCode);
    }

    client
        .getConnection()
        .close();
  }
}
