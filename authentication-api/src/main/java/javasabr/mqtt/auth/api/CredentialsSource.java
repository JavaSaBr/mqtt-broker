package javasabr.mqtt.auth.api;

import reactor.core.publisher.Mono;

public interface CredentialsSource {

  CredentialsSourceType getCredentialsSourceType();

  Mono<Boolean> isCredentialsExists(MqttCredentials credentials);
}
