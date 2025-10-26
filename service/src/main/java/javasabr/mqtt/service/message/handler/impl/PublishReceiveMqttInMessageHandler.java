package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.packet.MqttPacketType;
import javasabr.mqtt.network.packet.in.PublishReceivedInPacket;

public class PublishReceiveMqttInMessageHandler extends
    PendingOutResponseMqttInMessageHandler<PublishReceivedInPacket> {

  public PublishReceiveMqttInMessageHandler() {
    super(PublishReceivedInPacket.class);
  }

  @Override
  public MqttPacketType messageType() {
    return MqttPacketType.PUBLISH_RECEIVED;
  }
}
