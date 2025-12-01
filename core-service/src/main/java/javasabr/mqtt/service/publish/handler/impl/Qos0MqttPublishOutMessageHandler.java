package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;

public class Qos0MqttPublishOutMessageHandler extends AbstractMqttPublishOutMessageHandler<ExternalNetworkMqttUser> {

  public Qos0MqttPublishOutMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }

  @Override
  protected PublishHandlingResult handleImpl(Publish publish, ExternalNetworkMqttUser client) {
    startDelivering(client, publish);
    return PublishHandlingResult.SUCCESS;
  }
}
