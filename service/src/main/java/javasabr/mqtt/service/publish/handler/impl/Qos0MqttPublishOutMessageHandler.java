package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;

public class Qos0MqttPublishOutMessageHandler extends AbstractMqttPublishOutMessageHandler<ExternalMqttClient> {

  public Qos0MqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, subscriptionService, messageOutFactoryService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }

  @Override
  protected PublishHandlingResult handleImpl(PublishMqttInMessage packet, ExternalMqttClient client) {
    startDelivering(client, packet, packet.messageId(), false);
    return PublishHandlingResult.SUCCESS;
  }
}
