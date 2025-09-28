package javasabr.mqtt.service.handler.client;

import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.MqttSessionService;
import javasabr.mqtt.service.SubscriptionService;

public class DefaultMqttClientReleaseHandler extends AbstractMqttClientReleaseHandler<ExternalMqttClient> {

  public DefaultMqttClientReleaseHandler(
      ClientIdRegistry clientIdRegistry,
      MqttSessionService sessionService,
      SubscriptionService subscriptionService) {
    super(clientIdRegistry, sessionService, subscriptionService);
  }
}
