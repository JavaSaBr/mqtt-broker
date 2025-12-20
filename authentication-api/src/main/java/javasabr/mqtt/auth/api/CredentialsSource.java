package javasabr.mqtt.auth.api;

import reactor.core.publisher.Mono;

public interface CredentialsSource {

  String getName();

  Mono<Boolean> isCredentialsExists(String user, byte[] pass);
}
