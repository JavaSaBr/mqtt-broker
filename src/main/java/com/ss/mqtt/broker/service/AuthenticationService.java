package com.ss.mqtt.broker.service;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

    @NotNull Mono<Boolean> auth(@NotNull String userName, @NotNull byte[] password);

}
