package com.ss.mqtt.broker.service;

import com.ss.mqtt.broker.network.client.MqttClient;
import org.jetbrains.annotations.NotNull;

public interface PublishRetryService {

    void register(@NotNull MqttClient client);

    void unregister(@NotNull MqttClient client);

    boolean exist(@NotNull String clientId);
}
