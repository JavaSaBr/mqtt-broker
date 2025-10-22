package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.packet.MqttPacketType;
import javasabr.mqtt.network.packet.in.PublishReleaseInPacket;

public class PublishReleaseMqttInMessageHandler extends PendingResponseMqttInMessageHandler<PublishReleaseInPacket> {

  public PublishReleaseMqttInMessageHandler() {
    super(PublishReleaseInPacket.class);
  }

  @Override
  public int packetType() {
    return MqttPacketType.PUBLISH_RELEASED.typeIndex();
  }
}
