package javasabr.mqtt.service.handler.client;

import javasabr.mqtt.network.client.AbstractMqttClient;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.MqttSessionService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.network.handler.client.MqttClientReleaseHandler;
import javasabr.rlib.common.util.StringUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@CustomLog
@RequiredArgsConstructor
public abstract class AbstractMqttClientReleaseHandler<T extends AbstractMqttClient> implements
    MqttClientReleaseHandler {

  private final ClientIdRegistry clientIdRegistry;
  private final MqttSessionService sessionService;
  private final SubscriptionService subscriptionService;

  @Override
  public Mono<?> release(UnsafeMqttClient client) {
    var clientId = client.getClientId();
    //noinspection unchecked
    return releaseImpl((T) client).doOnNext(aVoid -> log.info(clientId, "Client:[%s] was released"::formatted));
  }

  protected Mono<?> releaseImpl(T client) {

    var clientId = client.getClientId();
    client.setClientId(StringUtils.EMPTY);

    if (StringUtils.isEmpty(clientId)) {
      log.warning(client, "This client:[%s] is already released or rejected"::formatted);
      return Mono.empty();
    }

    var session = client.getSession();

    Mono<?> asyncActions = null;

    if (session != null) {
      subscriptionService.cleanSubscriptions(client, session);
      if (client
          .getConnectionConfig()
          .isSessionsEnabled()) {
        asyncActions = sessionService.store(clientId, session, client.getSessionExpiryInterval());
        client.setSession(null);
      }
    }

    if (asyncActions != null) {
      asyncActions = asyncActions.flatMap(any -> clientIdRegistry.unregister(clientId));
    } else {
      asyncActions = clientIdRegistry.unregister(clientId);
    }

    return asyncActions;
  }
}
