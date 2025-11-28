package javasabr.mqtt.service.handler.client;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.network.MqttNetworkSession;
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler;
import javasabr.mqtt.network.impl.AbstractNetworkMqttUser;
import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.session.MqttSessionService;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class AbstractNetworkMqttUserReleaseHandler<T extends AbstractNetworkMqttUser> implements
    NetworkMqttUserReleaseHandler {

  ClientIdRegistry clientIdRegistry;
  MqttSessionService sessionService;
  SubscriptionService subscriptionService;

  @Override
  public Mono<?> release(ConfigurableNetworkMqttUser user) {
    var clientId = user.clientId();
    //noinspection unchecked
    return releaseImpl((T) user)
        .doOnNext(_ -> log.info(clientId, "[%s] Client was released"::formatted));
  }

  protected Mono<?> releaseImpl(T user) {

    String clientId = user.clientId();
    user.clientId(StringUtils.EMPTY);

    if (StringUtils.isEmpty(clientId)) {
      log.warning(user.clientId(), "[%s] This client is already released or rejected"::formatted);
      return Mono.empty();
    }

    MqttNetworkSession session = user.session();
    Mono<?> asyncActions = null;

    if (session != null) {
      subscriptionService.cleanSubscriptions(user, session);
      MqttClientConnectionConfig connectionConfig = user.connectionConfig();
      if (connectionConfig.sessionsEnabled()) {
        asyncActions = sessionService.store(clientId, session, connectionConfig.sessionExpiryInterval());
        user.session(null);
      }
    }

    if (asyncActions != null) {
      asyncActions = asyncActions.flatMap(_ -> clientIdRegistry.unregister(clientId));
    } else {
      asyncActions = clientIdRegistry.unregister(clientId);
    }

    return asyncActions;
  }
}
