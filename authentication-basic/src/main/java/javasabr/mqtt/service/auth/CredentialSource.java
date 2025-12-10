package javasabr.mqtt.service.auth;

import reactor.core.publisher.Mono;

public interface CredentialSource {

  String getName();

  Mono<Boolean> isCredentialExists(String user, byte[] pass);
}
