package com.ss.mqtt.broker.service;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface ClientIdRegistry {

    @NotNull Mono<Boolean> register(@NotNull String clientId);
    @NotNull Mono<Boolean> unregister(@NotNull String clientId);

    boolean validate(@NotNull String clientId);

    @NotNull Mono<String> generate();
}
