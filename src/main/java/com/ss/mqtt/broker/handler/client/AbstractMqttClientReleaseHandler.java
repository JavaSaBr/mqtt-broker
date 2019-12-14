package com.ss.mqtt.broker.handler.client;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.client.AbstractMqttClient;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.mqtt.broker.service.MqttSessionService;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
public abstract class AbstractMqttClientReleaseHandler<T extends AbstractMqttClient> implements
    MqttClientReleaseHandler {

    private final @NotNull ClientIdRegistry clientIdRegistry;
    private final @NotNull MqttSessionService sessionService;
    private final @NotNull SubscriptionService subscriptionService;

    @Override
    public @NotNull Mono<?> release(@NotNull UnsafeMqttClient client) {
        var clientId = client.getClientId();
        //noinspection unchecked
        return releaseImpl((T) client)
            .doOnNext(aVoid -> log.info("Client {} was released", clientId));
    }

    protected @NotNull Mono<?> releaseImpl(@NotNull T client) {

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
            if (client.getConnectionConfig().isSessionsEnabled()) {
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
