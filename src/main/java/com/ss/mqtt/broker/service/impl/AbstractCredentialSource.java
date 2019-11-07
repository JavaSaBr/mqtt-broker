package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.service.CredentialSource;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCredentialSource implements CredentialSource {

    private final Map<String, byte[]> credentials = new HashMap<>();

    abstract void init();

    void putCredentials(@NotNull Object user, @NotNull Object pass) {
        credentials.put(user.toString(), pass.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public @NotNull Mono<Boolean> check(@NotNull String user, @NotNull byte[] pass) {
        return Mono.just(Arrays.equals(pass, credentials.get(user)));
    }

    @Override
    public @NotNull Mono<Boolean> check(@NotNull byte[] pass) {
        return Mono.just(Boolean.FALSE);
    }
}
