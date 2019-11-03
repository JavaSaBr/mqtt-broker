package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.exception.ConnectionRejectException;
import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.service.AuthenticationService;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.mqtt.broker.service.CredentialSource;
import com.ss.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SimpleAuthenticationService implements AuthenticationService {

    private final @NotNull CredentialSource credentialSource;
    private final @NotNull ClientIdRegistry clientIdRegistry;
    private final boolean allowAnonymousAuth;

    @Override
    public @NotNull Mono<String> auth(@NotNull ConnectInPacket packet) {
        if (allowAnonymousAuth && !packet.isHasUserName()) {
            return register(packet);
        } else {
            return credentialSource.check(packet.getUsername(), packet.getPassword())
                .filter(Boolean::booleanValue)
                .flatMap(checkResult -> register(packet))
                .switchIfEmpty(Mono.error(new ConnectionRejectException(ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD)));
        }
    }

    private @NotNull Mono<String> register(@NotNull ConnectInPacket packet) {
        if (StringUtils.isEmpty(packet.getClientId())) {
            return clientIdRegistry.generate()
                .flatMap(clientId -> clientIdRegistry.register(clientId, packet.getUsername()));
        } else {
            return clientIdRegistry.register(packet.getClientId(), packet.getUsername());
        }
    }
}
