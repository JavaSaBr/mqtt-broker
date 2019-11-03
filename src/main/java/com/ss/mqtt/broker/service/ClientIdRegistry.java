package com.ss.mqtt.broker.service;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface ClientIdRegistry {

    @NotNull Mono<String> register(@NotNull String clientId, @NotNull String userName);
    @NotNull Mono<String> register(@NotNull String clientId);

    @NotNull Mono<Boolean> unregister(@NotNull String clientId);

    boolean validate(@NotNull String clientId);

    @NotNull Mono<String> generate();
}
