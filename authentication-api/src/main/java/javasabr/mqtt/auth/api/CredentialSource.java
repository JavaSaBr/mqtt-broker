package javasabr.mqtt.auth.api;

import reactor.core.publisher.Mono;

public interface CredentialSource {

  String getName();

  Mono<Boolean> isCredentialExists(String user, byte[] pass);
}
