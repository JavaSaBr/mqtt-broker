package javasabr.mqtt.legacy.service;

import reactor.core.publisher.Mono;

public interface CredentialSource {

  Mono<Boolean> check(String user, byte[] pass);

  Mono<Boolean> check(byte[] pass);
}
