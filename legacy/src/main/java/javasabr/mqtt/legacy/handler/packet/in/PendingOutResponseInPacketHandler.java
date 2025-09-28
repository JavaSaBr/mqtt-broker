package javasabr.mqtt.legacy.handler.packet.in;

import javasabr.mqtt.legacy.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.legacy.network.packet.HasPacketId;
import javasabr.mqtt.legacy.network.packet.in.MqttReadablePacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PendingOutResponseInPacketHandler<R extends MqttReadablePacket & HasPacketId> extends
    AbstractPacketHandler<UnsafeMqttClient, R> {

  @Override
  protected void handleImpl(UnsafeMqttClient client, R packet) {
    var session = client.getSession();
    if (session != null) {
      session.updateOutPendingPacket(client, packet);
    }
  }
}
