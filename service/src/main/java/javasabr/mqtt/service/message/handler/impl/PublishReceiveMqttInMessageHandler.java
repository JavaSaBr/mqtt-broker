package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage;

public class PublishReceiveMqttInMessageHandler extends
    PendingOutResponseMqttInMessageHandler<PublishReceivedMqttInMessage> {

  public PublishReceiveMqttInMessageHandler() {
    super(PublishReceivedMqttInMessage.class);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RECEIVED;
  }
}
