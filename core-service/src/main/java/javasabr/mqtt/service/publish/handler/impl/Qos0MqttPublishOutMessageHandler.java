package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;

public class Qos0MqttPublishOutMessageHandler extends AbstractMqttPublishOutMessageHandler<ExternalNetworkMqttUser> {

  public Qos0MqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, subscriptionService, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }

  @Override
  protected PublishHandlingResult handleImpl(Publish publish, ExternalNetworkMqttUser user) {
    startDelivering(user, publish);
    return PublishHandlingResult.SUCCESS;
  }
}
