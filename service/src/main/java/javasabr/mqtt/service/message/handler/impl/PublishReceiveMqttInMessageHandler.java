package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.packet.MqttPacketType;
import javasabr.mqtt.network.packet.in.PublishReceivedInPacket;

public class PublishReceiveMqttInMessageHandler extends PendingResponseMqttInMessageHandler<PublishReceivedInPacket> {

  public PublishReceiveMqttInMessageHandler() {
    super(PublishReceivedInPacket.class);
  }

  @Override
  public int packetType() {
    return MqttPacketType.PUBLISH_RECEIVED.typeIndex();
  }
}
