package javasabr.mqtt.legacy.handler.packet.in;

import javasabr.mqtt.legacy.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.legacy.network.packet.in.PublishReleaseInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishReleaseInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, PublishReleaseInPacket> {

  @Override
  protected void handleImpl(UnsafeMqttClient client, PublishReleaseInPacket packet) {
    var session = client.getSession();
    if (session != null) {
      session.updateInPendingPacket(client, packet);
    }
  }
}
