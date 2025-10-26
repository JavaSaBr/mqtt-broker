package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.packet.MqttPacketType;
import javasabr.mqtt.network.packet.in.PublishAckInPacket;

public class PublishAckMqttInMessageHandler extends PendingOutResponseMqttInMessageHandler<PublishAckInPacket> {

  public PublishAckMqttInMessageHandler() {
    super(PublishAckInPacket.class);
  }

  @Override
  public MqttPacketType messageType() {
    return MqttPacketType.PUBLISH_ACK;
  }
}
