package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishCompleteMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;

public class PublishCompleteMqttInMessageHandler extends
    PendingOutResponseMqttInMessageHandler<PublishCompleteMqttInMessage> {

  public PublishCompleteMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(PublishCompleteMqttInMessage.class, messageOutFactoryService);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_COMPLETED;
  }
}
