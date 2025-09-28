package javasabr.mqtt.network.handler.client;

import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import reactor.core.publisher.Mono;

public interface MqttClientReleaseHandler {

  Mono<?> release(UnsafeMqttClient client);
}
