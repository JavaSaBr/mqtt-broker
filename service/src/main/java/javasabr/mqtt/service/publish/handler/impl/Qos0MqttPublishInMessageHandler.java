package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;

public class Qos0MqttPublishInMessageHandler extends AbstractMqttPublishInMessageHandler<ExternalMqttClient> {

  public Qos0MqttPublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService) {
    super(ExternalMqttClient.class, subscriptionService, publishDeliveringService);
  }

  @Override
  public QoS qos() {
    return QoS.AT_MOST_ONCE;
  }
}
