package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.packet.in.PublishAckInPacket;

public class PublishAckMqttInMessageHandler extends PendingOutResponseMqttInMessageHandler<PublishAckInPacket> {

  public PublishAckMqttInMessageHandler() {
    super(PublishAckInPacket.class);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_ACK;
  }
}
