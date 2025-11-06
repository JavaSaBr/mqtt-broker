package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;

public class PublishReceiveMqttInMessageHandler extends
    PendingOutResponseMqttInMessageHandler<PublishReceivedMqttInMessage> {

  public PublishReceiveMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(PublishReceivedMqttInMessage.class, messageOutFactoryService);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RECEIVED;
  }
}
