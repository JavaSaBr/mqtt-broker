package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.packet.in.PublishReceivedInPacket;

public class PublishReceiveMqttInMessageHandler extends
    PendingOutResponseMqttInMessageHandler<PublishReceivedInPacket> {

  public PublishReceiveMqttInMessageHandler() {
    super(PublishReceivedInPacket.class);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RECEIVED;
  }
}
