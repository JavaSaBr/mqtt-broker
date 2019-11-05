package com.ss.mqtt.broker.service;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface CredentialSource {

    @NotNull Mono<Boolean> check(@NotNull String user, @NotNull byte[] pass);

    @NotNull Mono<Boolean> check(@NotNull byte[] pass);
}
