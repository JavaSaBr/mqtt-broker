package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;
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
  protected PublishHandlingResult handleImpl(PublishInPacket packet, ExternalMqttClient client) {
    startDelivering(client, packet, packet.getPacketId(), false);
    return PublishHandlingResult.SUCCESS;
  }
}
