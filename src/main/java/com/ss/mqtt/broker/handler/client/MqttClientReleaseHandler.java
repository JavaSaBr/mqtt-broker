package com.ss.mqtt.broker.handler.client;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface MqttClientReleaseHandler {

    @NotNull Mono<?> release(@NotNull UnsafeMqttClient client);
}
