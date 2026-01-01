package javasabr.mqtt.auth.api;

import reactor.core.publisher.Mono;

public interface AuthenticationService {
  Mono<Boolean> authenticate(MqttCredentials mqttCredentials);
}
