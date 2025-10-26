package javasabr.mqtt.service.handler.client;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.handler.MqttClientReleaseHandler;
import javasabr.mqtt.network.impl.AbstractMqttClient;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.session.MqttSessionService;
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
    var clientId = client.clientId();
    //noinspection unchecked
    return releaseImpl((T) client).doOnNext(aVoid -> log.info(clientId, "[%s] Client was released"::formatted));
  }

  protected Mono<?> releaseImpl(T client) {

    var clientId = client.clientId();
    client.clientId(StringUtils.EMPTY);

    if (StringUtils.isEmpty(clientId)) {
      log.warning(client.clientId(), "[%s] This client is already released or rejected"::formatted);
      return Mono.empty();
    }

    var session = client.session();

    Mono<?> asyncActions = null;

    if (session != null) {
      subscriptionService.cleanSubscriptions(client, session);
      MqttClientConnectionConfig connectionConfig = client.connectionConfig();
      if (connectionConfig.sessionsEnabled()) {
        asyncActions = sessionService.store(clientId, session, connectionConfig.sessionExpiryInterval());
        client.session(null);
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
