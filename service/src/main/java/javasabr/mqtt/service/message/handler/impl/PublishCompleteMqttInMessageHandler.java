package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.packet.MqttPacketType;
import javasabr.mqtt.network.packet.in.PublishCompleteInPacket;

public class PublishCompleteMqttInMessageHandler extends PendingResponseMqttInMessageHandler<PublishCompleteInPacket>  {

  public PublishCompleteMqttInMessageHandler() {
    super(PublishCompleteInPacket.class);
  }

  @Override
  public MqttPacketType messageType() {
    return MqttPacketType.PUBLISH_COMPLETED;
  }
}
