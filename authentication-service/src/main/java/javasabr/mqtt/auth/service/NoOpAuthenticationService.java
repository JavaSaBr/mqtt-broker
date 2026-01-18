package javasabr.mqtt.auth.service;

import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.auth.api.MqttCredentials;
import reactor.core.publisher.Mono;

public class NoOpAuthenticationService implements AuthenticationService {

  @Override
  public Mono<Boolean> authenticate(MqttCredentials mqttCredentials) {
    return Mono.just(true);
  }
}
