package javasabr.mqtt.service.handler.client;

import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.SessionService;
import javasabr.mqtt.service.SubscriptionService;

public class ExternalMqttClientReleaseHandler extends AbstractMqttClientReleaseHandler<ExternalMqttClient> {

  public ExternalMqttClientReleaseHandler(
      ClientIdRegistry clientIdRegistry,
      SessionService sessionService,
      SubscriptionService subscriptionService) {
    super(clientIdRegistry, sessionService, subscriptionService);
  }
}
