package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.service.AuthenticationService;
import com.ss.mqtt.broker.service.CredentialSource;
import com.ss.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SimpleAuthenticationService implements AuthenticationService {

    private final @NotNull CredentialSource credentialSource;
    private final boolean allowAnonymousAuth;

    @Override
    public @NotNull Mono<Boolean> auth(@NotNull String userName, @NotNull byte[] password) {
        if (allowAnonymousAuth && userName.equals(StringUtils.EMPTY)) {
            return Mono.just(Boolean.TRUE);
        } else {
            return credentialSource.check(userName, password);
        }
    }

}
