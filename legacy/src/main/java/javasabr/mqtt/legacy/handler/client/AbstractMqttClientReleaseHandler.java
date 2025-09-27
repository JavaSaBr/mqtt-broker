package javasabr.mqtt.legacy.handler.client;

import javasabr.mqtt.legacy.network.client.AbstractMqttClient;
import javasabr.mqtt.legacy.network.client.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.legacy.service.ClientIdRegistry;
import javasabr.mqtt.legacy.service.MqttSessionService;
import javasabr.mqtt.legacy.service.SubscriptionService;
import javasabr.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
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
    return releaseImpl((T) client).doOnNext(aVoid -> log.info("Client {} was released", clientId));
  }

  protected Mono<?> releaseImpl(T client) {

    var clientId = client.getClientId();
    client.setClientId(StringUtils.EMPTY);

    if (StringUtils.isEmpty(clientId)) {
      log.warn("This client {} is already released or rejected", client);
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
