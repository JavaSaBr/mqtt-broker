package com.ss.mqtt.broker.service;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface ClientIdRegistry {

    @NotNull CompletableFuture<Boolean> register(@NotNull String clientId);
    @NotNull CompletableFuture<Boolean> unregister(@NotNull String clientId);

    boolean validate(@NotNull String clientId);

    @NotNull CompletableFuture<String> generate();
}
