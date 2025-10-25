package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.packet.in.PublishCompleteInPacket;

public class PublishCompleteMqttInMessageHandler extends
    PendingOutResponseMqttInMessageHandler<PublishCompleteInPacket> {

  public PublishCompleteMqttInMessageHandler() {
    super(PublishCompleteInPacket.class);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_COMPLETED;
  }
}
