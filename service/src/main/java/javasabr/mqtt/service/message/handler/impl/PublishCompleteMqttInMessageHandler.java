package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishCompleteMqttInMessage;

public class PublishCompleteMqttInMessageHandler extends
    PendingOutResponseMqttInMessageHandler<PublishCompleteMqttInMessage> {

  public PublishCompleteMqttInMessageHandler() {
    super(PublishCompleteMqttInMessage.class);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_COMPLETED;
  }
}
