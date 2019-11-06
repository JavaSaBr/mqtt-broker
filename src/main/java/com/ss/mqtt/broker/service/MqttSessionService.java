package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.model.MqttSession;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface MqttSessionService {

    @NotNull Mono<MqttSession> getOrCreate(@NotNull String clientId);

    @NotNull Mono<MqttSession> createNew(@NotNull String clientId);

    @NotNull Mono<Boolean> store(@NotNull String clientId, @NotNull MqttSession session);
}
