package javasabr.mqtt.legacy.handler.client;

import javasabr.mqtt.legacy.network.client.MqttClient.UnsafeMqttClient;
import reactor.core.publisher.Mono;

public interface MqttClientReleaseHandler {

  Mono<?> release(UnsafeMqttClient client);
}
