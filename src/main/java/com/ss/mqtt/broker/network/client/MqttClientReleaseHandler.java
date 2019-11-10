package com.ss.mqtt.broker.network.client;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface MqttClientReleaseHandler {

    @NotNull Mono<?> release(@NotNull UnsafeMqttClient client);
}
