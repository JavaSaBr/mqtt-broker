package javasabr.mqtt.service.auth.source;

import reactor.core.publisher.Mono;

public interface CredentialSource {

  String getName();

  Mono<Boolean> isCredentialExists(String user, byte[] pass);
}
