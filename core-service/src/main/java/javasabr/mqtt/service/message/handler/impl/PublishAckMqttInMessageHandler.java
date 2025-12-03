package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;

public class PublishAckMqttInMessageHandler extends
    ProcessingOutPublishesMqttInMessageHandler<PublishAckMqttInMessage> {

  public PublishAckMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(PublishAckMqttInMessage.class, messageOutFactoryService);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_ACK;
  }
}
