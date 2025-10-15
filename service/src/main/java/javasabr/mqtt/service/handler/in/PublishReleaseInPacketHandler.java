package javasabr.mqtt.service.handler.in;

import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.packet.in.PublishReleaseInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishReleaseInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, PublishReleaseInPacket> {

  @Override
  protected void handleImpl(UnsafeMqttClient client, PublishReleaseInPacket packet) {
    var session = client.session();
    if (session != null) {
      session.updateInPendingPacket(client, packet);
    }
  }
}
