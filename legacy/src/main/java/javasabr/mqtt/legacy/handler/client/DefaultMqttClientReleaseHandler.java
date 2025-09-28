package javasabr.mqtt.legacy.handler.client;

import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.legacy.service.ClientIdRegistry;
import javasabr.mqtt.legacy.service.MqttSessionService;
import javasabr.mqtt.legacy.service.SubscriptionService;

public class DefaultMqttClientReleaseHandler extends AbstractMqttClientReleaseHandler<ExternalMqttClient> {

  public DefaultMqttClientReleaseHandler(
      ClientIdRegistry clientIdRegistry,
      MqttSessionService sessionService,
      SubscriptionService subscriptionService) {
    super(clientIdRegistry, sessionService, subscriptionService);
  }
}
