package javasabr.mqtt.service.handler.in;

import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.packet.HasPacketId;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PendingOutResponseInPacketHandler<R extends MqttReadablePacket & HasPacketId> extends
    AbstractPacketHandler<UnsafeMqttClient, R> {

  @Override
  protected void handleImpl(UnsafeMqttClient client, R packet) {
    var session = client.session();
    if (session != null) {
      session.updateOutPendingPacket(client, packet);
    }
  }
}
