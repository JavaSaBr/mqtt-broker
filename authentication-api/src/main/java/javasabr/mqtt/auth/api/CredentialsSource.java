package javasabr.mqtt.auth.api;

import reactor.core.publisher.Mono;

public interface CredentialsSource {

  CredentialsSourceType getType();

  Mono<Boolean> isCredentialsValid(MqttCredentials credentials);
}
