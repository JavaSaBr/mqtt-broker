package javasabr.mqtt.service.handler.client;

import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.session.MqttSessionService;

public class ExternalNetworkMqttUserReleaseHandler extends
    AbstractNetworkMqttUserReleaseHandler<ExternalNetworkMqttUser> {

  public ExternalNetworkMqttUserReleaseHandler(
      ClientIdRegistry clientIdRegistry,
      MqttSessionService sessionService,
      SubscriptionService subscriptionService) {
    super(clientIdRegistry, sessionService, subscriptionService);
  }
}
