package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage;

public class PublishAckMqttInMessageHandler extends PendingOutResponseMqttInMessageHandler<PublishAckMqttInMessage> {

  public PublishAckMqttInMessageHandler() {
    super(PublishAckMqttInMessage.class);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_ACK;
  }
}
