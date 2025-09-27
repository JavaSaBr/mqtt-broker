package javasabr.mqtt.legacy.handler.packet.in;

import javasabr.mqtt.legacy.network.client.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.legacy.network.packet.in.PublishInPacket;
import javasabr.mqtt.legacy.service.PublishingService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, PublishInPacket> {

  private final PublishingService publishingService;

  @Override
  protected void handleImpl(UnsafeMqttClient client, PublishInPacket packet) {
    publishingService.publish(client, packet);
  }
}
