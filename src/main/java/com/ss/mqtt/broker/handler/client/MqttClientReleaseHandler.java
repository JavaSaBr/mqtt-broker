package com.ss.mqtt.broker.handler.client;

import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import reactor.core.publisher.Mono;

public interface MqttClientReleaseHandler {

  Mono<?> release(UnsafeMqttClient client);
}
